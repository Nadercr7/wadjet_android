# Phase 03: API/Network Mismatches - Findings

## Source
Stages 4, 12 from 005-full-testing

## Issues Found
| # | ID | Category | Severity | File:Line | Description | Root Cause |
|---|-----|----------|----------|-----------|-------------|------------|
| 1 | FIX-030 | API | CRITICAL | core/network/LandmarkModels.kt | `parent`/`children` DTOs always null | Key name + type mismatch vs backend |
| 2 | FIX-031 | API | HIGH | core/network/WriteModels.kt | PaletteGroupsDto missing `numbers` + `determinative` | DTO has 4 fields, backend sends 6 |
| 3 | FIX-032 | API | MEDIUM | core/network/AuthInterceptor.kt | `/auth/logout` not in isAuthEndpoint() | Gets Bearer treatment instead of cookie |
| 4 | FIX-033 | Audio | HIGH | core/data/SettingsRepository + VMs | TTS settings decorative — never checked by playback | DataStore values ignored |
| 5 | FIX-034 | Audio | HIGH | 7 ViewModels | MediaPlayer code duplicated 7× — no audio focus | Copy-paste pattern |
| 6 | FIX-035 | Security | MEDIUM | app/src/main/res/xml/backup_rules.xml | Backup rules don't exclude encrypted_prefs | Token leak via ADB backup |
| 7 | FIX-036 | Security | LOW | app/proguard-rules.pro | `-renamesourcefileattribute` commented out | Real filenames in release |
| 8 | FIX-037 | Performance | LOW | app/WadjetApplication.kt:~L57 | Coil disk cache 5% of disk (3-6GB) | Percent-based |

## Fix Priority
| Priority | Issue IDs | Reason |
|----------|-----------|--------|
| P0 | FIX-030 | Landmark feature broken |
| P1 | FIX-031, FIX-033, FIX-034 | Missing features, TTS broken |
| P2 | FIX-032, FIX-035, FIX-036, FIX-037 | Security + polish |
