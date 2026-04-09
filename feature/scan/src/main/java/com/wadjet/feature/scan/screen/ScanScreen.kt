package com.wadjet.feature.scan.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.scan.ScanStep
import com.wadjet.feature.scan.ScanUiState
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    state: ScanUiState,
    onImageCaptured: (File) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { onImageSelected(it) } }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(WadjetColors.Night)) {
        if (state.cameraActive && hasCameraPermission) {
            CameraPreview(onImageCaptured = onImageCaptured)
            GoldBracketOverlay()
        } else if (!hasCameraPermission) {
            PermissionDeniedContent(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        }

        // Top bar
        TopAppBar(
            title = { Text("Scan", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            actions = {
                IconButton(onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, "Gallery", tint = WadjetColors.Gold)
                }
                IconButton(onClick = onNavigateToHistory) {
                    Icon(Icons.Default.History, "History", tint = WadjetColors.Gold)
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

        // Capture FAB
        if (state.cameraActive && hasCameraPermission && !state.isLoading) {
            FloatingActionButton(
                onClick = { /* Capture is handled by CameraPreview */ },
                containerColor = WadjetColors.Gold,
                contentColor = WadjetColors.Night,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .size(72.dp),
            ) {
                // Inner circle for capture button look
                Surface(
                    shape = CircleShape,
                    color = WadjetColors.Night,
                    modifier = Modifier.size(60.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WadjetColors.Gold,
                        modifier = Modifier.padding(4.dp),
                    ) {}
                }
            }
        }

        // Error
        state.error?.let { error ->
            Surface(
                color = WadjetColors.Surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptured: (File) -> Unit,
) {
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

            // Tap to capture
            previewView.setOnClickListener {
                val file = File(ctx.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
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

@Composable
private fun GoldBracketOverlay() {
    val gold = WadjetColors.Gold
    Canvas(modifier = Modifier.fillMaxSize()) {
        val bracketLen = 60.dp.toPx()
        val strokeW = 3.dp.toPx()
        val pad = 40.dp.toPx()
        val w = size.width
        val h = size.height

        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)

        // Top-left
        drawLine(gold, Offset(pad, pad), Offset(pad + bracketLen, pad), strokeWidth = strokeW)
        drawLine(gold, Offset(pad, pad), Offset(pad, pad + bracketLen), strokeWidth = strokeW)

        // Top-right
        drawLine(gold, Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), strokeWidth = strokeW)
        drawLine(gold, Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), strokeWidth = strokeW)

        // Bottom-left
        drawLine(gold, Offset(pad, h - pad), Offset(pad + bracketLen, h - pad), strokeWidth = strokeW)
        drawLine(gold, Offset(pad, h - pad), Offset(pad, h - pad - bracketLen), strokeWidth = strokeW)

        // Bottom-right
        drawLine(gold, Offset(w - pad, h - pad), Offset(w - pad - bracketLen, h - pad), strokeWidth = strokeW)
        drawLine(gold, Offset(w - pad, h - pad), Offset(w - pad, h - pad - bracketLen), strokeWidth = strokeW)
    }
}

@Composable
private fun ScanProgressOverlay(step: ScanStep) {
    val steps = listOf(
        ScanStep.DETECTING to "Detecting glyphs",
        ScanStep.CLASSIFYING to "Classifying signs",
        ScanStep.TRANSLITERATING to "Transliterating",
        ScanStep.TRANSLATING to "Translating",
    )

    val currentIndex = steps.indexOfFirst { it.first == step }
    val progress = if (currentIndex >= 0) (currentIndex + 1f) / steps.size else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulseAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WadjetColors.Night.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            CircularProgressIndicator(
                color = WadjetColors.Gold,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            steps.forEachIndexed { index, (stepEnum, label) ->
                val isActive = index == currentIndex
                val isDone = index < currentIndex
                val alpha = when {
                    isActive -> pulseAlpha
                    isDone -> 1f
                    else -> 0.4f
                }
                val color = when {
                    isDone -> WadjetColors.Gold
                    isActive -> WadjetColors.Gold.copy(alpha = alpha)
                    else -> WadjetColors.TextMuted
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    Text(
                        text = if (isDone) "●" else if (isActive) "●" else "○",
                        color = color,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = WadjetColors.Gold,
                trackColor = WadjetColors.Surface,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp),
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = WadjetColors.Gold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Wadjet needs camera access to scan hieroglyphs. Tap below to grant permission.",
            style = MaterialTheme.typography.bodyMedium,
            color = WadjetColors.TextMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        com.wadjet.core.designsystem.component.WadjetButton(
            text = "Grant Permission",
            onClick = onRequestPermission,
        )
    }
}
