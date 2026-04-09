package com.wadjet.feature.chat.screen

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.domain.model.ChatMessage
import com.wadjet.core.domain.model.ChatMessage.Role
import com.wadjet.feature.chat.ChatUiState
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onSpeak: (ChatMessage) -> Unit,
    onSttResult: (String) -> Unit,
    onSetRecording: (Boolean) -> Unit,
    onClearChat: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Local TTS fallback
    val localTts = remember { mutableMapOf<String, TextToSpeech?>() }
    val ttsInstance = remember {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts
    }

    DisposableEffect(Unit) {
        onDispose { ttsInstance?.shutdown() }
    }

    // Handle LOCAL_TTS error signal
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        if (error.startsWith("LOCAL_TTS:")) {
            val text = error.removePrefix("LOCAL_TTS:")
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            onDismissError()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content?.length) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // STT setup
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && speechRecognizer != null) {
            startListening(speechRecognizer, onSttResult, onSetRecording)
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    Scaffold(
        containerColor = WadjetColors.Night,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thoth",
                            color = WadjetColors.Gold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (state.landmarkSlug != null) {
                            Text(
                                text = "Discussing: ${state.landmarkSlug.replace("-", " ")}",
                                color = WadjetColors.Sand,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WadjetColors.Text,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onClearChat) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear chat",
                            tint = WadjetColors.TextMuted,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WadjetColors.Surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = state.messages,
                    key = { it.id },
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                    ) {
                        ChatBubble(
                            message = message,
                            isSpeaking = state.speakingMessageId == message.id,
                            onSpeak = { onSpeak(message) },
                        )
                    }
                }
            }

            // Input bar
            ChatInputBar(
                text = state.inputText,
                onTextChanged = onInputChanged,
                onSend = onSend,
                onMicTap = {
                    if (state.isRecording) {
                        speechRecognizer?.stopListening()
                        onSetRecording(false)
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isStreaming = state.isStreaming,
                isRecording = state.isRecording,
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == Role.USER

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUser) {
            // Ibis avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WadjetColors.Gold),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "𓅝",
                    fontSize = 18.sp,
                    color = WadjetColors.Night,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp,
                        ),
                    )
                    .background(if (isUser) WadjetColors.Gold else WadjetColors.Surface)
                    .padding(12.dp),
            ) {
                if (isUser) {
                    Text(
                        text = message.content,
                        color = WadjetColors.Night,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column {
                        if (message.content.isNotEmpty()) {
                            MarkdownText(
                                markdown = message.content,
                                color = WadjetColors.Text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (message.isStreaming) {
                            BlinkingCursor()
                        }
                    }
                }
            }

            // TTS button for bot messages
            if (!isUser && message.content.isNotEmpty() && !message.isStreaming) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = if (isSpeaking) {
                            Icons.Default.Stop
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = if (isSpeaking) "Stop" else "Listen",
                        tint = WadjetColors.Sand,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
    Text(
        text = "▌",
        color = WadjetColors.Gold.copy(alpha = alpha.value),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicTap: () -> Unit,
    isStreaming: Boolean,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WadjetColors.Surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = if (isRecording) "Listening..." else "Ask Thoth anything...",
                    color = WadjetColors.TextMuted,
                    fontStyle = if (isRecording) FontStyle.Italic else FontStyle.Normal,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = WadjetColors.SurfaceAlt,
                unfocusedContainerColor = WadjetColors.SurfaceAlt,
                focusedTextColor = WadjetColors.Text,
                unfocusedTextColor = WadjetColors.Text,
                cursorColor = WadjetColors.Gold,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            singleLine = false,
            maxLines = 4,
            enabled = !isStreaming,
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Mic button
        IconButton(
            onClick = onMicTap,
            enabled = !isStreaming,
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Voice input",
                tint = if (isRecording) WadjetColors.Error else WadjetColors.Sand,
            )
        }

        // Send button
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank() && !isStreaming,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank() && !isStreaming) WadjetColors.Gold else WadjetColors.TextMuted,
            )
        }
    }
}

private fun startListening(
    recognizer: SpeechRecognizer,
    onResult: (String) -> Unit,
    onRecording: (Boolean) -> Unit,
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onRecording(true)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            onResult(text)
        }

        override fun onError(error: Int) {
            onRecording(false)
        }

        override fun onEndOfSpeech() {}
        override fun onBeginningOfSpeech() {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
}
