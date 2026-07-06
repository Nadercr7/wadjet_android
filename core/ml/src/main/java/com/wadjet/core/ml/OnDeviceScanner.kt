package com.wadjet.core.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import com.wadjet.core.domain.model.ConfidenceSummary
import com.wadjet.core.domain.model.DetectedGlyph
import com.wadjet.core.domain.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * C2 (F-02): OFFLINE-ONLY on-device fallback for the scan pipeline.
 *
 * Server inference stays PRIMARY — this runs only when the server is
 * unreachable (see ScanRepositoryImpl). Uses the bundled ONNX models:
 *  - detector: YOLO26s end-to-end (NMS-free), in [1,3,640,640] /255,
 *    out [1,300,6] = [x1,y1,x2,y2,conf,class_id] in 640-letterbox space
 *  - classifier: MobileNetV3-Small, in [1,3,128,128] /255, out [1,171] logits,
 *    idx→Gardiner from label_mapping.json
 *
 * Deliberately produces NO transliteration/translation — those are server AI
 * capabilities; the result is flagged detectionSource="on_device_onnx" +
 * aiUnverified so the UI shows an honest "offline / lower accuracy" note.
 */
class OnDeviceScanner(private val context: Context) {

    private val env: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }

    private val detector: OrtSession by lazy {
        env.createSession(context.assets.open(DETECTOR_ASSET).use { it.readBytes() })
    }

    private val classifier: OrtSession by lazy {
        env.createSession(context.assets.open(CLASSIFIER_ASSET).use { it.readBytes() })
    }

    private val idxToGardiner: Map<Int, String> by lazy {
        val json = context.assets.open(LABELS_ASSET).use { it.readBytes() }.decodeToString()
        val mapping = JSONObject(json).getJSONObject("gardiner_to_idx")
        buildMap {
            mapping.keys().forEach { code -> put(mapping.getInt(code), code) }
        }
    }

    suspend fun scan(imageFile: File, mode: String): ScanResult = withContext(Dispatchers.Default) {
        val start = SystemClock.elapsedRealtime()
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: throw IllegalArgumentException("Could not decode image for on-device scan")

        val boxes = detect(bitmap)
        val detectionMs = (SystemClock.elapsedRealtime() - start).toDouble()

        val classifyStart = SystemClock.elapsedRealtime()
        val glyphs = boxes.mapNotNull { box ->
            val crop = cropWithMargin(bitmap, box) ?: return@mapNotNull null
            val (code, classConf) = classify(crop)
            DetectedGlyph(
                bbox = listOf(box.x1, box.y1, box.x2, box.y2),
                detectionConfidence = box.confidence,
                gardinerCode = code,
                classConfidence = classConf,
            )
        }
        val classificationMs = (SystemClock.elapsedRealtime() - classifyStart).toDouble()

        val ordered = readingOrder(glyphs)
        val confidences = ordered.map { it.classConfidence }
        Timber.i(
            "On-device scan: %d glyphs in %.0f+%.0f ms",
            ordered.size, detectionMs, classificationMs,
        )

        ScanResult(
            numDetections = ordered.size,
            glyphs = ordered,
            transliteration = null,
            gardinerSequence = ordered.joinToString("-") { it.gardinerCode }.ifEmpty { null },
            readingDirection = "left-to-right",
            layoutMode = null,
            translationEn = null,
            translationAr = null,
            detectionMs = detectionMs,
            classificationMs = classificationMs,
            transliterationMs = 0.0,
            translationMs = 0.0,
            totalMs = (SystemClock.elapsedRealtime() - start).toDouble(),
            mode = mode,
            detectionSource = SOURCE_ON_DEVICE,
            aiNotes = null,
            aiUnverified = true,
            qualityHints = emptyList(),
            confidenceSummary = if (confidences.isNotEmpty()) {
                ConfidenceSummary(
                    avg = confidences.average().toFloat(),
                    min = confidences.min(),
                    max = confidences.max(),
                    lowCount = confidences.count { it < 0.5f },
                )
            } else null,
        )
    }

    // ── detector ──

    private data class RawBox(
        val x1: Float, val y1: Float, val x2: Float, val y2: Float, val confidence: Float,
    )

    private fun detect(bitmap: Bitmap): List<RawBox> {
        // Letterbox to 640×640 preserving aspect ratio
        val scale = min(DET_SIZE.toFloat() / bitmap.width, DET_SIZE.toFloat() / bitmap.height)
        val newW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val newH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val dx = (DET_SIZE - newW) / 2f
        val dy = (DET_SIZE - newH) / 2f

        val letterboxed = Bitmap.createBitmap(DET_SIZE, DET_SIZE, Bitmap.Config.ARGB_8888)
        Canvas(letterboxed).apply {
            drawColor(Color.BLACK)
            drawBitmap(
                Bitmap.createScaledBitmap(bitmap, newW, newH, true),
                dx, dy, null,
            )
        }

        val input = bitmapToChwFloats(letterboxed, DET_SIZE)
        letterboxed.recycle()

        val inputName = detector.inputNames.first()
        OnnxTensor.createTensor(
            env, FloatBuffer.wrap(input), longArrayOf(1, 3, DET_SIZE.toLong(), DET_SIZE.toLong()),
        ).use { tensor ->
            detector.run(mapOf(inputName to tensor)).use { output ->
                @Suppress("UNCHECKED_CAST")
                val raw = output[0].value as Array<Array<FloatArray>> // [1][300][6]
                return raw[0]
                    .filter { it[4] >= CONFIDENCE_THRESHOLD }
                    .map { row ->
                        // un-letterbox back to source-image pixel space
                        RawBox(
                            x1 = ((row[0] - dx) / scale).coerceIn(0f, bitmap.width.toFloat()),
                            y1 = ((row[1] - dy) / scale).coerceIn(0f, bitmap.height.toFloat()),
                            x2 = ((row[2] - dx) / scale).coerceIn(0f, bitmap.width.toFloat()),
                            y2 = ((row[3] - dy) / scale).coerceIn(0f, bitmap.height.toFloat()),
                            confidence = row[4],
                        )
                    }
                    .filter { it.x2 - it.x1 >= 2f && it.y2 - it.y1 >= 2f }
            }
        }
    }

    // ── classifier ──

    private fun cropWithMargin(bitmap: Bitmap, box: RawBox): Bitmap? {
        val marginX = (box.x2 - box.x1) * CROP_MARGIN
        val marginY = (box.y2 - box.y1) * CROP_MARGIN
        val left = max(0, (box.x1 - marginX).toInt())
        val top = max(0, (box.y1 - marginY).toInt())
        val right = min(bitmap.width, (box.x2 + marginX).toInt())
        val bottom = min(bitmap.height, (box.y2 + marginY).toInt())
        if (right - left < 2 || bottom - top < 2) return null
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    private fun classify(crop: Bitmap): Pair<String, Float> {
        val resized = Bitmap.createScaledBitmap(crop, CLS_SIZE, CLS_SIZE, true)
        val input = bitmapToChwFloats(resized, CLS_SIZE)
        resized.recycle()
        crop.recycle()

        val inputName = classifier.inputNames.first()
        OnnxTensor.createTensor(
            env, FloatBuffer.wrap(input), longArrayOf(1, 3, CLS_SIZE.toLong(), CLS_SIZE.toLong()),
        ).use { tensor ->
            classifier.run(mapOf(inputName to tensor)).use { output ->
                @Suppress("UNCHECKED_CAST")
                val logits = (output[0].value as Array<FloatArray>)[0]
                var bestIdx = 0
                for (i in logits.indices) if (logits[i] > logits[bestIdx]) bestIdx = i
                // softmax probability of the argmax (numerically stable)
                val maxLogit = logits[bestIdx]
                val sumExp = logits.sumOf { exp((it - maxLogit).toDouble()) }
                val prob = (1.0 / sumExp).toFloat()
                return (idxToGardiner[bestIdx] ?: "?") to prob
            }
        }
    }

    // ── layout ──

    /** Row-band then left-to-right ordering — same simple grid rule the server uses. */
    private fun readingOrder(glyphs: List<DetectedGlyph>): List<DetectedGlyph> {
        if (glyphs.size < 2) return glyphs
        val heights = glyphs.map { it.bbox[3] - it.bbox[1] }.sorted()
        val bandHeight = max(1f, heights[heights.size / 2])
        return glyphs.sortedWith(
            compareBy(
                { ((it.bbox[1] + it.bbox[3]) / 2f / bandHeight).toInt() },
                { it.bbox[0] },
            ),
        )
    }

    private fun bitmapToChwFloats(bitmap: Bitmap, size: Int): FloatArray {
        val pixels = IntArray(size * size)
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)
        val out = FloatArray(3 * size * size)
        val plane = size * size
        for (i in pixels.indices) {
            val p = pixels[i]
            out[i] = ((p shr 16) and 0xFF) / 255f            // R
            out[plane + i] = ((p shr 8) and 0xFF) / 255f     // G
            out[2 * plane + i] = (p and 0xFF) / 255f         // B
        }
        return out
    }

    companion object {
        const val SOURCE_ON_DEVICE = "on_device_onnx"

        private const val DETECTOR_ASSET = "models/hieroglyph/detector/glyph_detector_uint8.onnx"
        private const val CLASSIFIER_ASSET = "models/hieroglyph/classifier/hieroglyph_classifier_uint8.onnx"
        private const val LABELS_ASSET = "models/hieroglyph/classifier/label_mapping.json"

        private const val DET_SIZE = 640
        private const val CLS_SIZE = 128

        // Between the server's permissive 0.15 (which has an AI safety net) and a
        // strict mobile setting — offline has no second opinion, so fewer false boxes.
        private const val CONFIDENCE_THRESHOLD = 0.30f
        private const val CROP_MARGIN = 0.08f
    }
}
