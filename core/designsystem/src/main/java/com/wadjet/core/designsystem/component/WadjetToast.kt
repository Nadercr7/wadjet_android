package com.wadjet.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import kotlinx.coroutines.delay

enum class ToastVariant { Success, Error, Info }

data class ToastState(
    val message: String,
    val variant: ToastVariant = ToastVariant.Info,
    val durationMs: Long = 3000L,
)

/**
 * Wadjet-branded toast notification.
 * Fixed bottom-center, success/error/info variants with colored left border.
 * Auto-dismisses after duration.
 */
@Composable
fun WadjetToast(
    toast: ToastState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast) {
        if (toast != null) {
            visible = true
            delay(toast.durationMs)
            visible = false
            delay(300) // Wait for exit animation
            onDismiss()
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
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
                    ToastVariant.Success -> "\u2713" // ✓
                    ToastVariant.Error -> "\u2717" // ✗
                    ToastVariant.Info -> "\u24D8" // ⓘ
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                    Text(
                        text = icon,
                        color = accentColor,
                        style = MaterialTheme.typography.titleMedium,
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
