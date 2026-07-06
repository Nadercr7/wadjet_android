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
