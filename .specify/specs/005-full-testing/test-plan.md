# Wadjet Android — Master Test Plan

## Testing Layers

| Layer | Tool | Scope | Estimated Count |
|-------|------|-------|-----------------|
| Unit | JUnit 4 + MockK + Turbine | ViewModels, Repos, Utils, Interceptors | ~200 |
| Integration | MockWebServer + Room in-memory | API→Repo→DB pipelines | ~20 |
| Screenshot | Roborazzi + Robolectric | Every screen × dark (+ RTL) | ~50 |
| Compose UI | compose-ui-test | Interactive flows (forms, nav) | ~20 |
| E2E | Emulator + Python Agent Scripts | Full user journeys | ~10 scripts |
| API Contract | curl / Python httpx | Backend alignment | ~37 |

**Total: ~337 new tests** (on top of existing 320 passing)

---

## Test Matrix Per Feature

| Feature | Unit | Integration | Screenshot | UI | E2E | Priority |
|---------|------|-------------|------------|-----|-----|----------|
| Auth (AuthVM) | ✅ 12 exist | — | Needed | Needed (login flow) | Needed (Journey 10) | P0 |
| Chat (ChatVM) | **0 → 15** | SSE mock | Needed | Needed (send+stream) | Needed (Journey 5) | P0 |
| Scan (ScanVM, ResultVM, HistoryVM) | **0 → 22** | — | Needed | Needed (upload) | Needed (Journey 2) | P0 |
| Dictionary (DictVM) | ✅ 9 exist | — | Needed | — | Needed (Journey 3) | P1 |
| Dictionary (SignDetail, Lesson, Translate, Write) | **0 → 20** | — | Needed | — | — | P2 |
| Explore (ExploreVM) | ✅ 9 exist | — | Needed | — | Needed (Journey 4) | P1 |
| Explore (DetailVM, IdentifyVM) | **0 → 12** | — | Needed | Needed (upload) | — | P2 |
| Stories (StoriesVM, ReaderVM) | **0 → 18** | — | Needed | — | Needed (Journey 6) | P1 |
| Dashboard (DashboardVM) | **0 → 10** | — | Needed | — | Needed (Journey 7) | P1 |
| Settings (SettingsVM) | **0 → 10** | — | Needed | — | Needed (Journey 8) | P1 |
| Landing (LandingVM) | **0 → 8** | — | Needed | — | — | P1 |
| Feedback (FeedbackVM) | **0 → 6** | — | Needed | — | — | P2 |
| HieroglyphsHub | **0 → 8** | — | Needed | — | — | P2 |
| Navigation | — | **0 → 12** | — | Nav test | — | P1 |
| Network (TokenManager) | **0 → 10** | — | — | — | — | P0 |
| Firebase (AuthManager) | **0 → 8** | — | — | — | — | P1 |
| Design System | ✅ 7 screenshots | — | Expand to all 19 comps | — | — | P2 |

---

## Phases

### Phase 1: Foundation (Must Have) — Test Infrastructure + P0 Units
1. Create `core/testing` module (MainDispatcherRule, test fixtures, fake repos)
2. Create custom `HiltTestRunner`
3. Write P0 unit tests: ChatVM, ScanVM, TokenManager (~45 tests)
4. Fix the 8 critical bugs found during testing

### Phase 2: Coverage (Should Have) — P1 Unit + Integration + Screenshots
1. Write P1 unit tests: StoriesVM, DashboardVM, SettingsVM, LandingVM, ScanResultVM, HistoryVM, FirebaseAuthManager, Navigation (~80 tests)
2. Expand Roborazzi to all feature modules — capture baselines for all 22 screens
3. Write navigation integration test with `@HiltAndroidTest`

### Phase 3: Depth (Good to Have) — P2 Units + Compose UI
1. Write P2 unit tests: SignDetailVM, LessonVM, TranslateVM, WriteVM, DetailVM, IdentifyVM, FeedbackVM, HieroglyphsHub (~75 tests)
2. Write Compose UI tests for critical interactive flows (login form, chat send, scan upload)
3. Add Arabic RTL screenshot variants

### Phase 4: Automation (Nice to Have) — E2E + Contract + CI
1. Write E2E emulator test scripts (10 user journeys)
2. Write API contract tests (37+ endpoints)
3. Integrate tests into CI/CD pipeline
4. Set up Roborazzi `verifyRoborazziDebug` as PR gate

---

## Success Criteria

| Metric | Current | Target |
|--------|---------|--------|
| Unit test count | 320 | 520+ |
| Module test coverage | 10/27 modules | 27/27 modules |
| ViewModel test coverage | 3/20 VMs | 20/20 VMs |
| Screenshot baselines | 7 (buttons only) | 50+ (all screens) |
| E2E scripts | 0 | 10 |
| CI test gate | None | All tests on PR |
