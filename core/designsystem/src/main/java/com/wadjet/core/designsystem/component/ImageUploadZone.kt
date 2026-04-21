package com.wadjet.core.designsystem.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.R
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.stringResource

@Composable
fun ImageUploadZone(
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    selectedImageUri: Uri? = null,
    title: String = "Tap to select an image",
    subtitle: String = "Supports JPG, PNG up to 10MB",
    analyzeButtonText: String = "Analyze",
    isAnalyzing: Boolean = false,
    onAnalyze: (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    browseButtonProminent: Boolean = false,
) {
    var localUri by remember { mutableStateOf(selectedImageUri) }
    LaunchedEffect(selectedImageUri) {
        localUri = selectedImageUri
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            localUri = it
            onImageSelected(it)
        }
    }

    val gold = WadjetColors.Gold
    val dashEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
    }

    if (localUri != null) {
        // Image selected state: preview + change/analyze
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
            ) {
                AsyncImage(
                    model = localUri,
                    contentDescription = stringResource(R.string.upload_selected_image_desc),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_placeholder_glyph),
                    error = painterResource(R.drawable.ic_placeholder_error),
                    fallback = painterResource(R.drawable.ic_placeholder_glyph),
                    modifier = Modifier.fillMaxSize(),
                )
                // "Change" overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(
                            WadjetColors.Night.copy(alpha = 0.7f),
                            MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.upload_change),
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (onAnalyze != null) {
                WadjetButton(
                    text = analyzeButtonText,
                    onClick = onAnalyze,
                    isLoading = isAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        // Empty state: dashed border upload zone
        Column(
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = gold,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = dashEffect,
                        ),
                    )
                }
                .clip(MaterialTheme.shapes.large)
                .clickable {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }
                .background(WadjetColors.Night)
                .padding(vertical = 48.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Icon (customizable or default Eye of Horus)
            if (icon != null) {
                icon()
            } else {
                Text(
                    text = "\uD80C\uDC80", // 𓂀 Eye of Horus
                    fontSize = 48.sp,
                    color = WadjetColors.Gold,
                    fontFamily = NotoSansEgyptianHieroglyphs,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WadjetColors.Text,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WadjetColors.TextMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (browseButtonProminent) {
                WadjetButton(
                    text = stringResource(R.string.upload_browse_gallery),
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                WadjetGhostButton(
                    text = stringResource(R.string.upload_browse_gallery),
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
            }
        }
    }
}
