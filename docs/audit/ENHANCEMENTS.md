# ENHANCEMENTS.md — proposals only (NOT implemented; need supervisor approval)

Format: What | Why it helps | Rough effort | Risk

_(collected during Phase 2)_

| # | What | Why it helps | Rough effort | Risk |
|---|---|---|---|---|
| E-P1 | Prefetch story bodies (not just summaries) in the background after the list loads, e.g. on unmetered network | E-02 currently makes only previously-opened stories readable offline; prefetch gives full offline reading | S (loop over cacheFullStory) | Low — more data usage; gate on Wi-Fi |
| E-P2 | Drive landmark paging off backend `has_more` (or adopt Paging 3) instead of computed totalPages | Removes reliance on a synthesized field; exact contract match with `explore.py` | S–M | Low |
| E-P3 | Replace fake scan progress overlay (fixed delays, F-04) with real stages from server `timing`/`detection_source` | Honest UX; shows real "AI Vision" source label | M | Low |
| E-P4 | Drop the dead Firestore story_progress mirror (writes are PERMISSION_DENIED in prod — see J-02) and rely on backend progress API | Removes a failing write + listener per story session, less log noise, faster saves | S | Low if rules really deny all clients (verified on emulator) — needs supervisor confirmation it's not env-specific |
| E-P5 | Migration test rig: keep a Gradle `room-testing` MigrationTestHelper test using exported schemas 7.json/8.json | Automates what was done by hand for E-01 (worktree v7 APK) | M | None (test-only) |
| E-P6 | Unify auth on the backend (drop Firebase Auth): use /auth/register//login//refresh//logout//send-verification//verify-email//forgot//reset and /auth/google with the Credential Manager ID token; delete the dead Firestore progress mirror | Removes dual-store drift (D-05), enables web-created accounts on Android, kills the broken email_verified divergence and the PERMISSION_DENIED Firestore writes (J-02) | L (full auth-stack refactor + full auth E2E re-verification) | Medium-high; must be its own release train |
| E-P7 | Backend: add a Pexels proxy endpoint, then remove PEXELS_API_KEY* from BuildConfig (J-01) | Removes extractable keys from release APKs | S backend + S Android | Low |
| E-P8 | Backend: host /.well-known/assetlinks.json so verify/reset email links open the app via App Links (B-01 follow-up) | Email links deep-link into the app instead of the browser | S backend + S Android | Low |

---

## Phase 3 status updates

- **E-P4** (drop Firestore mirror): SUPERSEDED — supervisor reversed to FIX; rules authored (A2), mirror stays.
- **E-P6** (backend-only auth): SUPERSEDED — supervisor reversed to Firebase-primary; implemented as A1.
- **E-P7** (Pexels proxy): **DONE** (backend `41c96c2` + Android `f1d53d0`).

## Prioritised shortlist for approval (top → bottom; none built)

| Pri | Id | What | Why it helps | Effort | Risk |
|---|---|---|---|---|---|
| 1 | E-P8 | Backend hosts `/.well-known/assetlinks.json` + Android https App Links for verify/reset/content URLs | Email links open the app (closes B-02); backend is now in scope so only the release-cert SHA-256 is needed from you | S+S | Low |
| 2 | E-P9 (new) | FCM push sender: backend endpoint/job reading `users/{uid}/fcm_tokens` (Admin SDK) to notify on new stories/landmarks | Makes A3 useful end-to-end — today nothing sends content pushes except manual console campaigns | M | Low |
| 3 | E-P1 | Wi-Fi prefetch of story bodies after list load | Full offline reading, not just previously-opened stories | S | Low |
| 4 | E-P5 | Room MigrationTestHelper rig using exported schemas 5..9 | Automates the by-hand v7→v8→v9 upgrade proofs | M | None |
| 5 | E-P3 | Real scan progress from server `timing`/`detection_source` stages | Honest, informative progress (F-04 removed the fakery; this adds real stages) | M | Low |
| 6 | E-P2 | Landmark paging via backend `has_more` (or Paging 3) | Exact contract match, removes synthesized totalPages | S–M | Low |
| 7 | E-P10 (new) | On-device landmark identify fallback (bundled EfficientNet-B0, 52 classes) mirroring the C2 pattern | Offline "what am I looking at", clearly lower-fidelity than the server AI pipeline | M | Low-Med |

## Phase 4 status updates

- **E-P1** (Wi-Fi story prefetch): **DONE** (Android `d79d6f1`) — verified 12/12 prefetched, offline reading of never-opened stories, NOT_METERED constraint, Settings toggle.
- **E-P8** (App Links): **DONE** (backend `99fef4b` + Android `b31bd9c`) — auto-verify PENDING-USER-DEPLOY (runbook §9 has the paste-ready env value).
- **E-P9** (push sender): **DONE** (backend `a941a84`+`99fef4b`) — real delivery PENDING-USER-CONSOLE (service account + rules deploy; runbook §7).

Remaining approved-nothing list (unchanged priorities): E-P5 (migration test rig), E-P3 (real scan progress stages), E-P2 (has_more paging), E-P10 (on-device landmark identify). New idea (propose-only): **E-P11** — hook `send_push` into content publishing (notify on new story) once the story catalog becomes dynamic; trivial once E-P9 credentials exist.
