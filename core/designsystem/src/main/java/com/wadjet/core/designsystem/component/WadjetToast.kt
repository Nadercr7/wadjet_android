package com.wadjet.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState

enum class ToastVariant { Success, Error, Info }

data class ToastState(
    val message: String,
    val variant: ToastVariant = ToastVariant.Info,
    val durationMs: Long = 3000L,
)

/**
 * Wadjet-branded toast notification.
 * Fixed bottom-center, success/error/info variants with colored left border.
 * Auto-dismisses after duration. Supports swipe-to-dismiss.
 */
@Composable
fun WadjetToast(
    toast: ToastState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 56.dp.toPx() }

    LaunchedEffect(toast) {
        if (toast != null) {
            offsetY = 0f
            visible = true
            delay(toast.durationMs)
            visible = false
            delay(300) // Wait for exit animation
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible && toast != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            toast?.let { state ->
                val accentColor = when (state.variant) {
                    ToastVariant.Success -> WadjetColors.Success
                    ToastVariant.Error -> WadjetColors.Error
                    ToastVariant.Info -> WadjetColors.Gold
                }

                val icon = when (state.variant) {
                    ToastVariant.Success -> Icons.Filled.CheckCircle
                    ToastVariant.Error -> Icons.Filled.Error
                    ToastVariant.Info -> Icons.Filled.Info
                }

                val iconDescription = when (state.variant) {
                    ToastVariant.Success -> "Success"
                    ToastVariant.Error -> "Error"
                    ToastVariant.Info -> "Info"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                if (delta > 0) offsetY += delta // only drag down
                            },
                            onDragStopped = {
                                if (offsetY > dismissThreshold) {
                                    visible = false
                                    onDismiss()
                                } else {
                                    offsetY = 0f
                                }
                            },
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .background(WadjetColors.Surface)
                        .drawBehind {
                            // Left accent border
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset.Zero,
                                size = Size(4.dp.toPx(), size.height),
                                cornerRadius = CornerRadius(2.dp.toPx()),
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WadjetColors.Text,
                    )
                }
            }
        }
    }
}
