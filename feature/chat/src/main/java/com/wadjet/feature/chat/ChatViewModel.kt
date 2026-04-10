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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val speakingMessageId: String? = null,
    val error: String? = null,
    val landmarkSlug: String? = null,
    val chatHistory: List<ConversationSummary> = emptyList(),
    val showHistory: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatHistoryStore: ChatHistoryStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val sessionId: String = UUID.randomUUID().toString()
    private var streamJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        // Check for landmark context from navigation
        val landmarkSlug = try {
            savedStateHandle.get<String>("slug")
        } catch (_: Exception) {
            null
        }
        if (landmarkSlug != null) {
            _state.update { it.copy(landmarkSlug = landmarkSlug) }
            // Auto-send a greeting about the landmark
            sendMessage("Tell me about this landmark")
        } else {
            // Add initial bot greeting
            val greeting = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = Role.ASSISTANT,
                content = "I am Thoth, keeper of wisdom and scribe of the gods. Ask me anything about ancient Egypt — hieroglyphs, pharaohs, temples, or the mysteries of the Nile.",
            )
            _state.update { it.copy(messages = listOf(greeting)) }
        }
        loadChatHistory()
    }

    fun updateInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String = _state.value.inputText) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isStreaming) return

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

            chatRepository.streamChat(
                message = trimmed,
                sessionId = sessionId,
                landmark = _state.value.landmarkSlug,
            )
                .catch { error ->
                    Timber.e(error, "Chat stream error")
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
    }

    fun speakMessage(message: ChatMessage) {
        if (_state.value.isSpeaking && _state.value.speakingMessageId == message.id) {
            stopSpeaking()
            return
        }
        stopSpeaking()

        _state.update { it.copy(isSpeaking = true, speakingMessageId = message.id) }

        viewModelScope.launch {
            chatRepository.speak(message.content).onSuccess { bytes ->
                if (bytes != null) {
                    playWavBytes(bytes, message.id)
                } else {
                    // 204 → signal UI to use local TTS
                    _state.update {
                        it.copy(
                            isSpeaking = false,
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
        _state.update { it.copy(isSpeaking = false, speakingMessageId = null) }
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
                _state.update { s -> s.copy(error = "STT_FALLBACK", isRecording = false) }
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
        chatHistoryStore.saveConversation(sessionId, _state.value.messages)
        viewModelScope.launch {
            chatRepository.clearSession(sessionId)
        }
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

    override fun onCleared() {
        super.onCleared()
        streamJob?.cancel()
        stopSpeaking()
    }
}
