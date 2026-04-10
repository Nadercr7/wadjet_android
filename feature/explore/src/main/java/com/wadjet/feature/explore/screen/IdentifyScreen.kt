package com.wadjet.feature.explore.screen

// CAMERA_DISABLED: CameraX imports commented out for image-upload-only mode
// import android.Manifest
// import android.content.pm.PackageManager
// import androidx.camera.core.CameraSelector
// import androidx.camera.core.ImageCapture
// import androidx.camera.core.ImageCaptureException
// import androidx.camera.core.Preview
// import androidx.camera.lifecycle.ProcessCameraProvider
// import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.component.BadgeVariant
import com.wadjet.core.designsystem.component.ImageUploadZone
import com.wadjet.core.designsystem.component.WadjetBadge
import com.wadjet.core.domain.model.IdentifyMatch
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.feature.explore.IdentifyUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifyScreen(
    state: IdentifyUiState,
    onImageCaptured: (java.io.File) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onMatchTap: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(WadjetColors.Night)) {
        // Main content: Image upload zone centered
        if (state.result == null && !state.isLoading) {
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
                        title = "Upload a photo of an Egyptian landmark",
                        subtitle = "Supports JPG, PNG up to 10MB",
                        analyzeButtonText = "Identify Landmark",
                        isAnalyzing = state.isLoading,
                        onAnalyze = null,
                    )
                }
            }
        }

        // Top bar
        TopAppBar(
            title = { Text("Identify Landmark", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        // Loading overlay
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WadjetColors.Night.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.wadjet.core.designsystem.component.WadjetSectionLoader(
                        text = "Identifying landmark...",
                    )
                }
            }
        }

        // Results
        if (state.result != null && !state.isLoading) {
            IdentifyResults(
                matches = state.result.matches,
                topDetail = if ((state.result.topMatch?.confidence ?: 0f) >= 0.80f) state.result.detail else null,
                onMatchTap = onMatchTap,
                onRetry = onRetry,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
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
                    message = error,
                    onRetry = onRetry,
                )
            }
        }
    }
}

// CAMERA_DISABLED: IdentifyCameraPreview — kept for future restoration
/*
@Composable
private fun IdentifyCameraPreview(onImageCaptured: (java.io.File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView.setOnClickListener {
                val file = java.io.File(ctx.cacheDir, "identify_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onImageCaptured(file)
                        }
                        override fun onError(exception: ImageCaptureException) {
                            timber.log.Timber.e(exception, "Image capture failed")
                        }
                    },
                )
            }
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}
*/

@Composable
private fun IdentifyResults(
    matches: List<IdentifyMatch>,
    topDetail: LandmarkDetail?,
    onMatchTap: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = WadjetColors.Surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (matches.isEmpty()) "No landmarks identified" else "Top Matches",
                style = MaterialTheme.typography.titleMedium,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            // Inline preview for high-confidence top match
            if (topDetail != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WadjetColors.Night,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMatchTap(topDetail.slug) },
                ) {
                    Column {
                        topDetail.thumbnail?.let { url ->
                            coil3.compose.AsyncImage(
                                model = url,
                                contentDescription = topDetail.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(WadjetColors.Surface),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            )
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = topDetail.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = WadjetColors.Gold,
                                fontWeight = FontWeight.Bold,
                            )
                            topDetail.type?.let { type ->
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WadjetColors.Sand,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (matches.isEmpty()) {
                Text(
                    "Try a different angle or get closer to the landmark.",
                    color = WadjetColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                com.wadjet.core.designsystem.component.WadjetButton(
                    text = "Try Again",
                    onClick = onRetry,
                )
            } else {
                matches.forEachIndexed { index, match ->
                    FadeUp(visible = true) {
                        MatchCard(
                            match = match,
                            rank = index + 1,
                            onClick = { onMatchTap(match.slug) },
                        )
                    }
                    if (index < matches.lastIndex) Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(12.dp))
                com.wadjet.core.designsystem.component.WadjetButton(
                    text = "Scan Again",
                    onClick = onRetry,
                )
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: IdentifyMatch,
    rank: Int,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WadjetColors.Night,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // Rank circle
            Surface(
                shape = CircleShape,
                color = if (rank == 1) WadjetColors.Gold else WadjetColors.Surface,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "$rank",
                        color = if (rank == 1) WadjetColors.Night else WadjetColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WadjetColors.Text,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            // Confidence badge
            val confPct = (match.confidence * 100).toInt()
            val variant = when {
                confPct >= 80 -> BadgeVariant.Success
                confPct >= 50 -> BadgeVariant.Gold
                else -> BadgeVariant.Error
            }
            WadjetBadge(text = "$confPct%", variant = variant)
        }
    }
}
