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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ScaffoldDefaults
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
import com.wadjet.core.designsystem.component.StreamingDots
import com.wadjet.core.domain.model.ChatMessage
import com.wadjet.core.domain.model.ChatMessage.Role
import com.wadjet.feature.chat.ChatUiState
import com.wadjet.feature.chat.ConversationSummary
import androidx.compose.ui.res.stringResource
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.chat.R
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.chat_title),
                            color = WadjetColors.Gold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (state.landmarkSlug != null) {
                            val displayName = state.landmarkSlug.replace("-", " ")
                                .split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                            Text(
                                text = stringResource(R.string.chat_discussing, displayName),
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
                            contentDescription = stringResource(DesignR.string.action_back),
                            tint = WadjetColors.Text,
                        )
                    }
                },
                actions = {
                    if (state.chatHistory.isNotEmpty()) {
                        IconButton(onClick = onToggleHistory) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.chat_history_action),
                                tint = if (state.showHistory) WadjetColors.Gold else WadjetColors.TextMuted,
                            )
                        }
                    }
                    IconButton(onClick = onClearChat) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.chat_clear_action),
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
                            text = stringResource(R.string.chat_history_count, state.chatHistory.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = WadjetColors.TextMuted,
                        )
                        Text(
                            text = stringResource(R.string.chat_clear_all),
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
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onLoadConversation(convo.id) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.chat_convo_history_desc),
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
                                    text = stringResource(R.string.chat_message_count, convo.messageCount),
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
                                title = stringResource(R.string.chat_empty_title),
                                subtitle = stringResource(R.string.chat_empty_subtitle),
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
                            contentDescription = stringResource(R.string.chat_scroll_to_bottom_desc),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // Quick suggestion chips (first message only — greeting + no user messages yet)
            val hasOnlyGreeting = state.messages.size <= 1 && !state.isStreaming
            if (hasOnlyGreeting) {
                val suggestions = listOf(
                    stringResource(R.string.chat_suggestion_pyramids),
                    stringResource(R.string.chat_suggestion_hieroglyphs),
                    stringResource(R.string.chat_suggestion_pharaohs),
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
                        modifier = Modifier.navigationBarsPadding().imePadding(),
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
                    modifier = Modifier.navigationBarsPadding().imePadding(),
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
    val context = LocalContext.current
    val thothDesc = stringResource(R.string.chat_thoth_avatar_desc)

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
                    modifier = Modifier.semantics { contentDescription = thothDesc },
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
                        text = { Text(stringResource(DesignR.string.action_copy)) },
                        onClick = {
                            onCopy()
                            showMenu = false
                        },
                    )
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
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
                    text = remember(message.timestamp) { formatRelativeTime(context, message.timestamp) },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = WadjetColors.TextMuted,
                )
                if (isUser && onEdit != null && !message.isStreaming) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.chat_edit_message_desc),
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
                        contentDescription = stringResource(R.string.chat_error_desc),
                        tint = WadjetColors.Error,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.chat_retry),
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
                            contentDescription = if (isSpeaking) stringResource(DesignR.string.action_stop) else stringResource(DesignR.string.action_listen),
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
                    text = stringResource(R.string.chat_thoth_thinking),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.width(6.dp))
                StreamingDots()
            }
        }
    }
}

private fun formatRelativeTime(context: android.content.Context, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> context.getString(R.string.time_just_now)
        diff < 3_600_000 -> context.getString(R.string.time_minutes_ago, diff / 60_000)
        diff < 86_400_000 -> context.getString(R.string.time_hours_ago, diff / 3_600_000)
        diff < 172_800_000 -> context.getString(R.string.time_yesterday)
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
                    text = stringResource(R.string.chat_editing_banner),
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Gold,
                )
                Text(
                    text = stringResource(DesignR.string.action_cancel),
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
                text = stringResource(R.string.chat_char_count, text.length),
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
                    text = if (isRecording) stringResource(R.string.chat_input_listening) else stringResource(R.string.chat_input_placeholder),
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
            shape = MaterialTheme.shapes.extraLarge,
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
                contentDescription = if (isRecording) stringResource(R.string.chat_stop_recording_desc) else stringResource(R.string.chat_voice_input_desc),
                tint = if (isRecording) WadjetColors.Error else WadjetColors.Sand,
            )
        }

        // Send or Stop button
        if (isStreaming) {
            IconButton(onClick = onStopStreaming) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.chat_stop_generating_desc),
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
                    contentDescription = stringResource(DesignR.string.action_send),
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
