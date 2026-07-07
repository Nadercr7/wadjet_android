# DESIGN C2 — On-device ML as OFFLINE-ONLY fallback + 16KB alignment (F-02 / A-03)

Date: 2026-07-06 · Phase 3 · Status: DESIGN (implementation follows on isolated commits)

## Decision (supervisor)

Wire the shipped ONNX models so they WORK, with zero damage to the working server scan:
server inference stays PRIMARY (accurate, full pipeline); on-device runs ONLY when the server
is unreachable, clearly labelled "offline / lower accuracy". Also fix the 16KB-page-size Play
blocker by upgrading ONNX Runtime.

## What we have (verified)

| Model | Asset | Contract (from bundled model_metadata.json) |
|---|---|---|
| Glyph detector | `assets/models/hieroglyph/detector/glyph_detector_uint8.onnx` (9.9MB) | YOLO26s end-to-end, `nms_free: true`, input NCHW `[1,3,640,640]` /255, output `[1,300,6]` = `[x1,y1,x2,y2,conf,class_id]`, 1 class |
| Glyph classifier | `assets/models/hieroglyph/classifier/hieroglyph_classifier_uint8.onnx` (1.8MB) | MobileNetV3-Small, input `[1,3,128,128]` /255, output `[1,171]` logits; `label_mapping.json` maps Gardiner code ↔ index |
| Landmark classifier | `assets/models/landmark/landmark_classifier_uint8.onnx` (4.4MB) | EfficientNet-B0, input 224, 52 classes |

`:core:ml` is an empty stub that only pulls `onnxruntime-android:1.20.0` (source of the
unaligned `.so` files → A-03).

## Scope

- **In scope**: hieroglyph scan offline fallback (detector → per-box classifier), ONNX Runtime
  16KB upgrade. This is the user-visible "Scan" feature.
- **Out of scope this phase**: landmark on-device classification (the explore identify flow is a
  server AI pipeline with rich text output that has no offline analog; a 52-class label alone is
  a worse product than the offline landmark LIST which already works from seed data). The model
  stays bundled; proposal recorded in ENHANCEMENTS.

## Architecture (zero-damage)

New in `:core:ml` (real module at last):

- `OnnxSession` — lazily creates `OrtEnvironment`/`OrtSession` from an asset path (models stay
  in `app` assets; byte-copied to `cacheDir/ml` on first use, SHA-checked). Sessions cached;
  closed on process death only (singleton).
- `GlyphDetector.detect(bitmap): List<RawBox>` — letterbox to 640×640 (preserve aspect, pad),
  RGB float /255 NCHW; parse `[1,300,6]`; keep `conf >= 0.35` (server's own default detection
  threshold); un-letterbox coords back to source-bitmap space.
- `GlyphClassifier.classify(crop): Pair<gardinerCode, confidence>` — crop box (+8% margin,
  clamped), resize 128×128, /255 NCHW, argmax over 171 logits (softmax for confidence),
  `label_mapping.json` idx→Gardiner.
- `OnDeviceScanner.scan(imageBytes): ScanResult` — decode → detect → classify each box (cap 300)
  → assemble a domain `ScanResult`:
  - `glyphs` with bbox + `detection_confidence` + `gardiner_code` + `class_confidence`
  - `gardinerSequence` in reading order (sort by row bands then x — same simple layout rule the
    server uses for `layout_mode=grid`), `numDetections`
  - **`detectionSource = "on_device_onnx"`**, `aiUnverified = true`
  - transliteration/translation left null — the on-device models cannot produce them; the UI
    copy makes this explicit rather than faking it.
  - Sign names enriched from the local Room `signs` table (seeded offline in E-04) by Gardiner
    code, so the result list is still informative with zero network.

Integration point — `ScanRepositoryImpl.scanImage` ONLY:

```
try server scan (unchanged)                 ← PRIMARY, byte-identical behavior online
catch IOException / connectivity failure →  run OnDeviceScanner, mark on-device
(non-IO failures — HTTP 4xx/5xx — still surface as errors exactly as today;
 the fallback fires only when the server cannot be REACHED)
```

UX note: `ScanResultScreen` already renders a source badge from `detectionSource` (it has an
`onnx` branch waiting). Add an explicit banner string for `on_device_onnx`:
EN "Offline result — on-device detection (lower accuracy). Translation needs a connection."
AR equivalent; shown only for on-device results. Scan history stores the source as today.

Daily scan limit (`getLimits()`) is a server call — offline it already fails soft; the fallback
path skips the limit check (offline scans don't consume server quota by definition).

## 16KB alignment (A-03)

Bump `onnxRuntime = 1.20.0 → 1.27.0` (latest stable, June 2026). The JNI-lib alignment fix
(`-Wl,-z,max-page-size=16384` for `libonnxruntime4j_jni.so`) merged upstream 2025-06-04 and
ships in ≥1.23.0. Verification is EMPIRICAL, not release-notes-based:
1. Parse ELF program headers of every `.so` in the built APK — all `PT_LOAD p_align` must be
   ≥ 0x4000 for arm64-v8a.
2. `assembleRelease` must produce no 16KB-alignment warning.
3. Emulator smoke: scan online (server path regression) + airplane-mode scan (on-device path).

## Accuracy honesty

The models are uint8-quantized mobile models; the supervisor decision explicitly acknowledges
LOW ACCURACY. We (a) measure sanity on the emulator with a real hieroglyph test image (expect:
plausible box count, non-degenerate Gardiner codes), (b) never let the offline result silently
masquerade as a server result (source stored + badged + banner), and (c) if output proves
unusable garbage on the test image, we keep the wiring strictly offline-gated and say exactly
that in the report (per instructions).

## Risk & rollback

New code is additive in `:core:ml` + one guarded catch-branch in `ScanRepositoryImpl` + strings.
Online behavior is untouched by construction. Version bump is one line in `libs.versions.toml`;
ORT 1.20→1.27 API surface used here (create session / run / close) is stable. Each piece is its
own commit; reverting the repository commit restores today's scan exactly.
