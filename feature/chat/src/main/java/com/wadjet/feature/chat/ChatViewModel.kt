package com.wadjet.feature.chat

import android.media.MediaPlayer
import android.speech.SpeechRecognizer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wadjet.core.domain.model.ChatMessage
import com.wadjet.core.domain.model.ChatMessage.Role
import com.wadjet.core.domain.repository.ChatRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val isRecording: Boolean = false,
    val isSpeaking: Boolean = false,
    val isLoadingTts: Boolean = false,
    val speakingMessageId: String? = null,
    val error: String? = null,
    val landmarkSlug: String? = null,
    val chatHistory: List<ConversationSummary> = emptyList(),
    val showHistory: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatHistoryStore: ChatHistoryStore,
    private val toastController: com.wadjet.core.common.ToastController,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val sessionId: String
    private var streamJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var lastSentMessage: String? = null

    companion object {
        private const val STREAM_TIMEOUT_MS = 60_000L
    }

    init {
        // Resolve or create session
        val landmarkSlug = try {
            savedStateHandle.get<String>("slug")
        } catch (_: Exception) {
            null
        }

        if (landmarkSlug != null) {
            // Landmark-specific chat always gets a fresh session
            sessionId = UUID.randomUUID().toString()
            chatHistoryStore.storeSessionId(sessionId)
            _state.update { it.copy(landmarkSlug = landmarkSlug) }
            sendMessage("Tell me about this landmark")
        } else {
            // Try to resume an existing session
            val activeId = chatHistoryStore.getActiveSessionId()
            if (activeId != null) {
                sessionId = activeId
                val restored = chatHistoryStore.loadConversation(activeId)
                if (!restored.isNullOrEmpty()) {
                    _state.update { it.copy(messages = restored) }
                } else {
                    addGreeting()
                }
            } else {
                sessionId = UUID.randomUUID().toString()
                chatHistoryStore.storeSessionId(sessionId)
                addGreeting()
            }
        }
        loadChatHistory()
    }

    private fun addGreeting() {
        val greeting = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.ASSISTANT,
            content = "I am Thoth, keeper of wisdom and scribe of the gods. Ask me anything about ancient Egypt — hieroglyphs, pharaohs, temples, or the mysteries of the Nile.",
        )
        _state.update { it.copy(messages = listOf(greeting)) }
    }

    fun updateInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String = _state.value.inputText) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isStreaming) return

        viewModelScope.launch {
            // Check free-tier limits
            userRepository.getLimits().onSuccess { limits ->
                if (limits.chatMessagesToday >= limits.chatMessagesPerDay) {
                    toastController.error("Daily message limit reached (${limits.chatMessagesPerDay}). Try again tomorrow.")
                    _state.update {
                        it.copy(error = "Daily message limit reached (${limits.chatMessagesPerDay}). Try again tomorrow.")
                    }
                    return@launch
                }
            }

            doSendMessage(trimmed)
        }
    }

    private fun doSendMessage(trimmed: String) {
        lastSentMessage = trimmed

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.USER,
            content = trimmed,
        )

        val botMessageId = UUID.randomUUID().toString()
        val botMessage = ChatMessage(
            id = botMessageId,
            role = Role.ASSISTANT,
            content = "",
            isStreaming = true,
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage + botMessage,
                inputText = "",
                isStreaming = true,
                error = null,
            )
        }

        streamJob = viewModelScope.launch {
            val contentBuilder = StringBuilder()

            val completed = withTimeoutOrNull(STREAM_TIMEOUT_MS) {
                chatRepository.streamChat(
                    message = trimmed,
                    sessionId = sessionId,
                    landmark = _state.value.landmarkSlug,
                )
                    .catch { error ->
                        Timber.e(error, "Chat stream error")
                        toastController.error("Failed to send message")
                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == botMessageId) {
                                        msg.copy(
                                            content = contentBuilder.toString().ifEmpty {
                                                "Sorry, I encountered an error. Please try again."
                                            },
                                            isStreaming = false,
                                        )
                                    } else {
                                        msg
                                    }
                                },
                                isStreaming = false,
                                error = error.message,
                            )
                        }
                    }
                    .onCompletion {
                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == botMessageId) {
                                        msg.copy(isStreaming = false)
                                    } else {
                                        msg
                                    }
                                },
                                isStreaming = false,
                            )
                        }
                    }
                    .collect { chunk ->
                        contentBuilder.append(chunk)
                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == botMessageId) {
                                        msg.copy(content = contentBuilder.toString())
                                    } else {
                                        msg
                                    }
                                },
                            )
                        }
                    }
            }

            // Timeout triggered — cancel and show error
            if (completed == null) {
                _state.update { state ->
                    state.copy(
                        messages = state.messages.map { msg ->
                            if (msg.id == botMessageId) {
                                msg.copy(
                                    content = contentBuilder.toString().ifEmpty {
                                        "Connection timed out. Please try again."
                                    },
                                    isStreaming = false,
                                )
                            } else {
                                msg
                            }
                        },
                        isStreaming = false,
                        error = "Connection timed out",
                    )
                }
            }
        }
    }

    fun retryLastMessage() {
        val last = lastSentMessage ?: return
        // Remove the last failed bot message
        _state.update { state ->
            val messages = state.messages.toMutableList()
            if (messages.isNotEmpty() && messages.last().role == Role.ASSISTANT) {
                messages.removeAt(messages.lastIndex)
            }
            if (messages.isNotEmpty() && messages.last().role == Role.USER) {
                messages.removeAt(messages.lastIndex)
            }
            state.copy(messages = messages, error = null)
        }
        doSendMessage(last)
    }

    fun speakMessage(message: ChatMessage) {
        if (_state.value.isSpeaking && _state.value.speakingMessageId == message.id) {
            stopSpeaking()
            return
        }
        stopSpeaking()

        _state.update { it.copy(isSpeaking = true, isLoadingTts = true, speakingMessageId = message.id) }

        toastController.info("Generating audio\u2026")

        viewModelScope.launch {
            chatRepository.speak(message.content).onSuccess { bytes ->
                if (bytes != null) {
                    _state.update { it.copy(isLoadingTts = false) }
                    playWavBytes(bytes, message.id)
                } else {
                    // 204 → signal UI to use local TTS
                    _state.update {
                        it.copy(
                            isSpeaking = false,
                            isLoadingTts = false,
                            speakingMessageId = null,
                            error = "LOCAL_TTS:${message.content}",
                        )
                    }
                }
            }.onFailure { error ->
                Timber.e(error, "TTS failed")
                _state.update {
                    it.copy(
                        isSpeaking = false,
                        isLoadingTts = false,
                        speakingMessageId = null,
                        error = "LOCAL_TTS:${message.content}",
                    )
                }
            }
        }
    }

    private fun playWavBytes(bytes: ByteArray, messageId: String) {
        try {
            val tempFile = File.createTempFile("tts_", ".wav")
            tempFile.writeBytes(bytes)
            tempFile.deleteOnExit()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    _state.update { it.copy(isSpeaking = false, speakingMessageId = null) }
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                }
                setOnErrorListener { _, _, _ ->
                    _state.update { it.copy(isSpeaking = false, speakingMessageId = null) }
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "MediaPlayer failed")
            _state.update { it.copy(isSpeaking = false, speakingMessageId = null) }
        }
    }

    fun stopSpeaking() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _state.update { it.copy(isSpeaking = false, isLoadingTts = false, speakingMessageId = null) }
    }

    fun onSttResult(text: String) {
        _state.update { it.copy(inputText = text, isRecording = false) }
    }

    fun transcribeAudio(audioFile: File) {
        viewModelScope.launch {
            chatRepository.transcribe(audioFile).onSuccess { text ->
                if (text.isNotBlank()) {
                    _state.update { it.copy(inputText = text, isRecording = false) }
                }
            }.onFailure {
                Timber.e(it, "Server STT failed, use local fallback")
                _state.update { s ->
                    s.copy(
                        error = "Voice-to-text unavailable on server. Tap the mic button to use on-device speech recognition.",
                        isRecording = false,
                    )
                }
            }
        }
    }

    fun setRecording(recording: Boolean) {
        _state.update { it.copy(isRecording = recording) }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        _state.update { s ->
            val msgs = s.messages.map { m ->
                if (m.isStreaming) m.copy(isStreaming = false) else m
            }
            s.copy(messages = msgs, isStreaming = false)
        }
    }

    fun clearChat() {
        streamJob?.cancel()
        stopSpeaking()
        // Save current conversation to history before clearing
        saveCurrentConversation()
        viewModelScope.launch {
            chatRepository.clearSession(sessionId)
        }
        // Clear stored session so next init creates fresh
        chatHistoryStore.clearSessionId()
        toastController.success("Chat cleared")
        val greeting = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.ASSISTANT,
            content = "Chat cleared. How can I help you?",
        )
        _state.update {
            ChatUiState(
                messages = listOf(greeting),
                landmarkSlug = it.landmarkSlug,
                chatHistory = chatHistoryStore.listConversations(),
            )
        }
    }

    fun toggleHistory() {
        _state.update { it.copy(showHistory = !it.showHistory) }
    }

    fun loadConversation(conversationId: String) {
        val messages = chatHistoryStore.loadConversation(conversationId) ?: return
        _state.update {
            it.copy(messages = messages, showHistory = false)
        }
    }

    fun clearHistory() {
        chatHistoryStore.clearAll()
        _state.update { it.copy(chatHistory = emptyList()) }
    }

    private fun loadChatHistory() {
        _state.update { it.copy(chatHistory = chatHistoryStore.listConversations()) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun saveCurrentConversation() {
        chatHistoryStore.saveConversation(sessionId, _state.value.messages)
    }

    override fun onCleared() {
        super.onCleared()
        streamJob?.cancel()
        saveCurrentConversation()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
