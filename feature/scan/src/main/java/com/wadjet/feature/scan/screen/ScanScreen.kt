package com.wadjet.feature.scan.screen

// CAMERA_DISABLED: CameraX imports commented out for image-upload-only mode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.shineSweep
import com.wadjet.core.designsystem.component.ImageUploadZone
import com.wadjet.feature.scan.ScanStep
import com.wadjet.feature.scan.ScanUiState
import com.wadjet.feature.scan.R
import com.wadjet.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    state: ScanUiState,
    onImageCaptured: (java.io.File) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(WadjetColors.Night)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            FadeUp(visible = true) {
                ImageUploadZone(
                    onImageSelected = onImageSelected,
                    title = stringResource(R.string.scan_upload_title),
                    subtitle = stringResource(R.string.scan_upload_subtitle),
                    analyzeButtonText = stringResource(R.string.scan_analyze_button),
                    isAnalyzing = state.isLoading,
                    onAnalyze = null,
                )
            }
        }

        // Top bar
        TopAppBar(
            title = { Text(stringResource(R.string.scan_title), color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), tint = WadjetColors.Gold)
                }
            },
            actions = {
                IconButton(onClick = onNavigateToHistory) {
                    Icon(Icons.Default.History, stringResource(R.string.scan_history_action), tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )

        // Loading overlay
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            ScanProgressOverlay(step = state.scanStep)
        }

        // CAMERA_DISABLED: Capture FAB removed — image upload replaces camera capture

        // Error
        state.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                com.wadjet.core.designsystem.component.ErrorState(
                    message = error ?: stringResource(R.string.scan_error_fallback),
                )
            }
        }
    }
}

@Composable
private fun ScanProgressOverlay(step: ScanStep) {
    val steps = listOf(
        ScanStep.DETECTING to (stringResource(R.string.scan_step_detecting) to stringResource(R.string.scan_step_detecting_sub)),
        ScanStep.CLASSIFYING to (stringResource(R.string.scan_step_classifying) to stringResource(R.string.scan_step_classifying_sub)),
        ScanStep.TRANSLITERATING to (stringResource(R.string.scan_step_transliterating) to stringResource(R.string.scan_step_transliterating_sub)),
        ScanStep.TRANSLATING to (stringResource(R.string.scan_step_translating) to stringResource(R.string.scan_step_translating_sub)),
    )

    val currentIndex = steps.indexOfFirst { it.first == step }

    // Animated gold shimmer bar
    val shimmerProgress = remember { Animatable(0f) }
    LaunchedEffect(currentIndex) {
        val target = if (currentIndex >= 0) (currentIndex + 1f) / steps.size else 0f
        shimmerProgress.animateTo(target, animationSpec = tween(600, easing = EaseInOut))
    }

    // Gold shimmer on the progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerBar")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerBarOffset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WadjetColors.Night.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
        ) {
            // Hieroglyph branding icon
            val eyeDesc = stringResource(R.string.scan_progress_eye_desc)
            Text(
                "𓂀",
                fontSize = 48.sp,
                color = WadjetColors.Gold,
                modifier = Modifier.semantics { contentDescription = eyeDesc },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.scan_progress_title),
                style = MaterialTheme.typography.titleMedium,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Pipeline steps
            steps.forEachIndexed { index, (_, labelPair) ->
                val (label, subtitle) = labelPair
                val isActive = index == currentIndex
                val isDone = index < currentIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    // Step indicator
                    val indicatorColor = when {
                        isDone -> WadjetColors.Gold
                        isActive -> WadjetColors.Gold
                        else -> WadjetColors.TextMuted.copy(alpha = 0.4f)
                    }
                    Text(
                        text = when {
                            isDone -> "✓"
                            isActive -> "◉"
                            else -> "○"
                        },
                        color = indicatorColor,
                        fontSize = if (isActive) 20.sp else 16.sp,
                        fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            color = when {
                                isDone -> WadjetColors.Gold
                                isActive -> WadjetColors.Text
                                else -> WadjetColors.TextMuted.copy(alpha = 0.5f)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (isActive) {
                            Text(
                                text = subtitle,
                                color = WadjetColors.Sand,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                // Connector line between steps
                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .width(2.dp)
                            .height(12.dp)
                            .background(
                                if (isDone) WadjetColors.Gold.copy(alpha = 0.5f)
                                else WadjetColors.Border,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gold shimmer progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(WadjetColors.Surface),
            ) {
                // Filled portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shimmerProgress.value)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .drawBehind {
                            val w = size.width
                            val highlightCenter = shimmerOffset * w
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        WadjetColors.Gold,
                                        WadjetColors.GoldLight,
                                        WadjetColors.Gold,
                                    ),
                                    startX = highlightCenter - w * 0.3f,
                                    endX = highlightCenter + w * 0.3f,
                                ),
                            )
                        },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${((shimmerProgress.value) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.Sand,
            )
        }
    }
}

// U8: removed dead PermissionDeniedContent (unreachable since the camera was
// disabled; it carried hardcoded English strings). Scan is image-upload-only.
