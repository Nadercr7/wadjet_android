package com.wadjet.feature.chat.screen

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.borderBeam
import com.wadjet.core.designsystem.component.StreamingDots
import com.wadjet.core.domain.model.ChatMessage
import com.wadjet.core.domain.model.ChatMessage.Role
import com.wadjet.feature.chat.ChatUiState
import com.wadjet.feature.chat.ConversationSummary
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onSpeak: (ChatMessage) -> Unit,
    onRetry: () -> Unit,
    onStartEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSttResult: (String) -> Unit,
    onSetRecording: (Boolean) -> Unit,
    onTranscribeAudio: (File) -> Unit,
    onStopStreaming: () -> Unit,
    onClearChat: () -> Unit,
    onToggleHistory: () -> Unit,
    onLoadConversation: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
            val isArabic = text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
            ttsInstance?.language = if (isArabic) Locale("ar") else Locale.US
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            onDismissError()
        }
    }

    // Show non-TTS errors via toast
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        if (!error.startsWith("LOCAL_TTS:")) {
            // Error is displayed via toast from ViewModel already; just dismiss state
            onDismissError()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content?.length) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // STT setup — prefer Android SpeechRecognizer, fall back to server (Groq Whisper)
    val hasLocalStt = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember {
        if (hasLocalStt) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    // Server STT (MediaRecorder → Groq Whisper) for devices without SpeechRecognizer
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            if (hasLocalStt && speechRecognizer != null) {
                startListening(speechRecognizer, onSttResult, onSetRecording)
            } else {
                // Server STT: record with MediaRecorder
                try {
                    val file = File(context.cacheDir, "stt_${System.currentTimeMillis()}.ogg")
                    @Suppress("DEPRECATION")
                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(context)
                    } else {
                        MediaRecorder()
                    }
                    recorder.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.OGG)
                        setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                        setOutputFile(file.absolutePath)
                        prepare()
                        start()
                    }
                    mediaRecorder = recorder
                    recordingFile = file
                    onSetRecording(true)
                } catch (_: Exception) {
                    onSetRecording(false)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            mediaRecorder?.apply { try { stop(); release() } catch (_: Exception) {} }
        }
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
                            val displayName = state.landmarkSlug.replace("-", " ")
                                .split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                            Text(
                                text = "Discussing: $displayName",
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
                    if (state.chatHistory.isNotEmpty()) {
                        IconButton(onClick = onToggleHistory) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Past conversations",
                                tint = if (state.showHistory) WadjetColors.Gold else WadjetColors.TextMuted,
                            )
                        }
                    }
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
            // Past conversations panel
            if (state.showHistory && state.chatHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WadjetColors.Surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Past conversations (${state.chatHistory.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = WadjetColors.TextMuted,
                        )
                        Text(
                            text = "Clear all",
                            style = MaterialTheme.typography.labelSmall,
                            color = WadjetColors.Sand,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onClearHistory() }
                                .padding(4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    state.chatHistory.take(10).forEach { convo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLoadConversation(convo.id) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Conversation history",
                                tint = WadjetColors.Sand,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = convo.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WadjetColors.Text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${convo.messageCount} messages",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WadjetColors.TextMuted,
                                )
                            }
                        }
                    }
                }
            }

            // Message list + scroll-to-bottom FAB
            val isAtBottom = remember {
                derivedStateOf {
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisible >= state.messages.size - 1
                }
            }
            val clipboard = LocalContext.current.getSystemService(android.content.ClipboardManager::class.java)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            com.wadjet.core.designsystem.component.EmptyState(
                                glyph = "\uD80C\uDD5D",
                                title = "Ask Thoth anything",
                                subtitle = "Questions about ancient Egypt, hieroglyphs, pharaohs, and more",
                                modifier = Modifier.fillParentMaxHeight(0.6f),
                            )
                        }
                    }
                    items(
                        items = state.messages,
                        key = { it.id },
                    ) { message ->
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically { it / 2 },
                        ) {
                            ChatBubble(
                                message = message,
                                isSpeaking = state.speakingMessageId == message.id,
                                isLoadingTts = state.isLoadingTts && state.speakingMessageId == message.id,
                                isLastBotMessage = message.role == Role.ASSISTANT &&
                                    state.messages.lastOrNull { it.role == Role.ASSISTANT } == message,
                                hasError = state.error != null && !state.error.orEmpty().startsWith("LOCAL_TTS:"),
                                isEditing = state.editingMessageId == message.id,
                                onSpeak = { onSpeak(message) },
                                onCopy = {
                                    clipboard?.setPrimaryClip(
                                        android.content.ClipData.newPlainText("message", message.content),
                                    )
                                },
                                onEdit = if (message.role == Role.USER) {
                                    { onStartEdit(message.id) }
                                } else {
                                    null
                                },
                                onRetry = onRetry,
                            )
                        }
                    }
                    // Typing indicator
                    if (state.isStreaming && state.messages.lastOrNull()?.content?.isEmpty() == true) {
                        item(key = "typing") {
                            TypingIndicator()
                        }
                    }
                }

                // Scroll-to-bottom FAB
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isAtBottom.value && state.messages.size > 3,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp),
                    enter = fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                ) {
                    androidx.compose.material3.SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(state.messages.size - 1)
                            }
                        },
                        containerColor = WadjetColors.Gold,
                        contentColor = WadjetColors.Night,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // Quick suggestion chips (first message only — greeting + no user messages yet)
            val hasOnlyGreeting = state.messages.size <= 1 && !state.isStreaming
            if (hasOnlyGreeting) {
                val isArabic = Locale.getDefault().language == "ar"
                val suggestions = if (isArabic) listOf(
                    "أخبرني عن الأهرامات",
                    "ما هي الهيروغليفية؟",
                    "فراعنة مشهورين",
                ) else listOf(
                    "Tell me about the pyramids",
                    "What are hieroglyphs?",
                    "Famous pharaohs",
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(suggestions) { suggestion ->
                        FilterChip(
                            selected = false,
                            onClick = { onInputChanged(suggestion); onSend() },
                            label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = WadjetColors.Surface,
                                labelColor = WadjetColors.Gold,
                            ),
                        )
                    }
                }
            }

            // Input bar with BorderBeam when streaming
            val micTapAction: () -> Unit = {
                if (state.isRecording) {
                    if (hasLocalStt) {
                        speechRecognizer?.stopListening()
                        onSetRecording(false)
                    } else {
                        // Stop server recording and transcribe
                        try { mediaRecorder?.apply { stop(); release() } } catch (_: Exception) {}
                        mediaRecorder = null
                        val file = recordingFile
                        if (file != null && file.exists() && file.length() > 0) {
                            onTranscribeAudio(file)
                        }
                        onSetRecording(false)
                    }
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            if (state.isStreaming) {
                com.wadjet.core.designsystem.animation.BorderBeam(durationMs = 2000) {
                    ChatInputBar(
                        text = state.inputText,
                        onTextChanged = onInputChanged,
                        onSend = onSend,
                        onMicTap = micTapAction,
                        onStopStreaming = onStopStreaming,
                        isStreaming = state.isStreaming,
                        isRecording = state.isRecording,
                        isEditing = false,
                        onCancelEdit = onCancelEdit,
                    )
                }
            } else {
                ChatInputBar(
                    text = state.inputText,
                    onTextChanged = onInputChanged,
                    onSend = onSend,
                    onMicTap = micTapAction,
                    onStopStreaming = onStopStreaming,
                    isStreaming = state.isStreaming,
                    isRecording = state.isRecording,
                    isEditing = state.editingMessageId != null,
                    onCancelEdit = onCancelEdit,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    isLoadingTts: Boolean = false,
    isLastBotMessage: Boolean = false,
    hasError: Boolean = false,
    isEditing: Boolean = false,
    onSpeak: () -> Unit,
    onCopy: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == Role.USER
    var showMenu by remember { mutableStateOf(false) }

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
                    modifier = Modifier.semantics { contentDescription = "Thoth" },
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Box {
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
                        .background(
                            if (isEditing) WadjetColors.Gold.copy(alpha = 0.7f)
                            else if (isUser) WadjetColors.Gold
                            else WadjetColors.Surface,
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showMenu = true },
                        )
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
                                StreamingDots(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                // Copy popup
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        onClick = {
                            onCopy()
                            showMenu = false
                        },
                    )
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                        )
                    }
                }
            }

            // Timestamp + edit icon for user messages
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp),
            ) {
                Text(
                    text = formatRelativeTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = WadjetColors.TextMuted,
                )
                if (isUser && onEdit != null && !message.isStreaming) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit message",
                        tint = WadjetColors.TextMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .clickable { onEdit() },
                    )
                }
            }

            // Error + retry (last bot message with error, content is error text)
            if (!isUser && isLastBotMessage && hasError && !message.isStreaming) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = WadjetColors.Error,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.Gold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onRetry() }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }

            // TTS button for bot messages
            if (!isUser && message.content.isNotEmpty() && !message.isStreaming) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(48.dp),
                ) {
                    if (isLoadingTts) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = WadjetColors.Sand,
                        )
                    } else {
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
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
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
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(WadjetColors.Surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Thoth is thinking",
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.width(6.dp))
                StreamingDots()
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicTap: () -> Unit,
    onStopStreaming: () -> Unit,
    isStreaming: Boolean,
    isRecording: Boolean,
    isEditing: Boolean = false,
    onCancelEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth()) {
        // Edit mode banner
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WadjetColors.Gold.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Editing message",
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Gold,
                )
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Sand,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCancelEdit() }
                        .padding(4.dp),
                )
            }
        }
        // Character counter
        if (text.isNotEmpty()) {
            Text(
                text = "${text.length}/2000",
                color = if (text.length > 1800) WadjetColors.Error else WadjetColors.TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp, bottom = 2.dp),
            )
        }
    Row(
        modifier = Modifier
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

        // Send or Stop button
        if (isStreaming) {
            IconButton(onClick = onStopStreaming) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop generating",
                    tint = WadjetColors.Error,
                )
            }
        } else {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onSend()
                },
                enabled = text.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) WadjetColors.Gold else WadjetColors.TextMuted,
                )
            }
        }
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
