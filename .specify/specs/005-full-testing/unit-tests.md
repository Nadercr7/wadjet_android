# Unit Tests Specification

> Match existing patterns: JUnit 4, MockK, Turbine, backtick test names, `StandardTestDispatcher` + `advanceUntilIdle()`.

---

## Module: core:network

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 1 | `TokenManagerTest` | `save and read access token` | `assertEquals(token, tokenManager.getAccessToken())` | P0 |
| 2 | `TokenManagerTest` | `save and read refresh token via cookie` | `assertEquals(cookie, tokenManager.getRefreshCookie())` | P0 |
| 3 | `TokenManagerTest` | `clear tokens removes all` | `assertNull(tokenManager.getAccessToken())` after clear | P0 |
| 4 | `TokenManagerTest` | `concurrent reads are thread-safe` | Multiple coroutines read same value without crash | P0 |
| 5 | `TokenManagerTest` | `tokens survive process recreation` | Re-create TokenManager, tokens still present | P0 |
| 6 | `TokenManagerTest` | `empty EncryptedSharedPreferences returns null` | `assertNull` on fresh instance | P0 |
| 7 | `TokenManagerTest` | `save overwrites previous token` | Second save wins | P1 |
| 8 | `TokenManagerTest` | `clear while reading doesn't crash` | Concurrent clear + read → no exception | P1 |

## Module: core:firebase

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 9 | `FirebaseAuthManagerTest` | `signInWithCustomToken succeeds` | `assertTrue(manager.isAuthenticated)` | P1 |
| 10 | `FirebaseAuthManagerTest` | `signOut clears Firebase user` | `assertNull(manager.currentUser)` | P1 |
| 11 | `FirebaseAuthManagerTest` | `getIdToken returns valid token` | `assertNotNull(result)` | P1 |
| 12 | `FirebaseAuthManagerTest` | `signIn failure emits error` | `assertIs<AuthState.Error>(state)` | P1 |
| 13 | `FirebaseAuthManagerTest` | `getCurrentUser returns null when not signed in` | `assertNull(manager.currentUser)` | P1 |
| 14 | `FirebaseAuthManagerTest` | `signOut when not signed in is no-op` | No exception thrown | P1 |

## Module: feature:chat

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 15 | `ChatViewModelTest` | `initial state has empty messages and suggestions` | `assertEquals(emptyList(), state.messages)`; suggestions non-empty | P0 |
| 16 | `ChatViewModelTest` | `sendMessage adds user message to list` | `assertEquals("hello", state.messages.last().text)` | P0 |
| 17 | `ChatViewModelTest` | `sendMessage with empty text is no-op` | Messages list unchanged | P0 |
| 18 | `ChatViewModelTest` | `sendMessage during streaming is blocked` | Second send ignored while `isStreaming=true` | P0 |
| 19 | `ChatViewModelTest` | `streaming response appends bot message` | Bot message appears after user message | P0 |
| 20 | `ChatViewModelTest` | `streaming error sets error state` | `assertNotNull(state.error)` | P0 |
| 21 | `ChatViewModelTest` | `editMessage replaces message at index` | `assertEquals(newText, state.messages[idx].text)` | P0 |
| 22 | `ChatViewModelTest` | `retryMessage re-sends last user message` | API called again with same text | P0 |
| 23 | `ChatViewModelTest` | `clearChat resets messages and calls API` | `assertEquals(emptyList(), state.messages)` | P1 |
| 24 | `ChatViewModelTest` | `speakMessage calls audio API with message text` | `coVerify { chatRepo.speak(text, any()) }` | P1 |
| 25 | `ChatViewModelTest` | `speakMessage while already speaking stops previous` | `isSpeaking` toggles correctly | P1 |
| 26 | `ChatViewModelTest` | `dismissError clears error state` | `assertNull(state.error)` | P1 |
| 27 | `ChatViewModelTest` | `onSttResult populates input field` | `assertEquals(transcript, state.input)` | P1 |
| 28 | `ChatViewModelTest` | `loadConversationHistory restores messages` | Messages populated from history | P1 |
| 29 | `ChatViewModelTest` | `GlobalScope in onCleared does not leak` | Verify no coroutine outlives test | P0 |

## Module: feature:scan

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 30 | `ScanViewModelTest` | `initial state has no image and not loading` | `assertNull(state.imageUri)`; `assertFalse(state.isLoading)` | P0 |
| 31 | `ScanViewModelTest` | `onImageCaptured sets loading and calls scan API` | `assertTrue(state.isLoading)`; `coVerify { scanRepo.scanImage(any()) }` | P0 |
| 32 | `ScanViewModelTest` | `onImageCaptured during loading is no-op (double-submit guard)` | API called exactly once | P0 |
| 33 | `ScanViewModelTest` | `scan success navigates to result` | Navigation event emitted with scan ID | P0 |
| 34 | `ScanViewModelTest` | `scan error sets error state` | `assertNotNull(state.error)` | P0 |
| 35 | `ScanViewModelTest` | `speak calls audio API` | `coVerify { scanRepo.speak(any()) }` | P1 |
| 36 | `ScanViewModelTest` | `dismissError clears error` | `assertNull(state.error)` | P1 |
| 37 | `ScanResultViewModelTest` | `init loads scan result from saved state handle` | `assertEquals(expected, state.scanResult)` | P1 |
| 38 | `ScanResultViewModelTest` | `load error shows error state` | `assertNotNull(state.error)` | P1 |
| 39 | `ScanResultViewModelTest` | `speak calls audio with transliteration` | `coVerify { scanRepo.speak(any()) }` | P1 |
| 40 | `ScanResultViewModelTest` | `retry reloads scan result` | API called again | P1 |
| 41 | `HistoryViewModelTest` | `init loads scan history` | `assertEquals(expected, state.history)` | P2 |
| 42 | `HistoryViewModelTest` | `deleteScan removes item from list` | Item removed from state | P2 |
| 43 | `HistoryViewModelTest` | `deleteScan during loading is no-op` | No double-delete | P2 |
| 44 | `HistoryViewModelTest` | `delete error shows error to user` | `assertNotNull(state.error)` | P2 |

## Module: feature:stories

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 45 | `StoriesViewModelTest` | `init loads stories list` | `assertTrue(state.stories.isNotEmpty())` | P1 |
| 46 | `StoriesViewModelTest` | `selectFilter filters by difficulty` | Only matching stories shown | P1 |
| 47 | `StoriesViewModelTest` | `toggleStoryFavorite with guard prevents rapid toggle` | Only 1 API call on double-tap | P1 |
| 48 | `StoriesViewModelTest` | `refresh reloads stories` | API called again, list updated | P1 |
| 49 | `StoriesViewModelTest` | `error state set on network failure` | `assertNotNull(state.error)` | P1 |
| 50 | `StoryReaderViewModelTest` | `init loads story from savedStateHandle` | `assertEquals(storyId, state.story.id)` | P1 |
| 51 | `StoryReaderViewModelTest` | `goToChapter updates current chapter index` | `assertEquals(2, state.currentChapter)` | P1 |
| 52 | `StoryReaderViewModelTest` | `submitAnswer with correct answer shows positive feedback` | `assertTrue(state.feedbackCorrect)` | P1 |
| 53 | `StoryReaderViewModelTest` | `submitAnswer with no loading guard is fixed` | Only 1 submission per interaction | P1 |
| 54 | `StoryReaderViewModelTest` | `speakChapter calls audio API per paragraph` | Audio API called | P1 |
| 55 | `StoryReaderViewModelTest` | `GlobalScope in onCleared does not leak` | No coroutine outlives test | P1 |
| 56 | `StoryReaderViewModelTest` | `loadChapterImage concurrent calls handled` | Only 1 image load at a time | P1 |
| 57 | `StoryReaderViewModelTest` | `glyphAnnotation tap records learning` | Interaction recorded | P2 |
| 58 | `StoryReaderViewModelTest` | `error loading story shows error state` | Error state set | P1 |

## Module: feature:dashboard

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 59 | `DashboardViewModelTest` | `init loads user stats` | `assertNotNull(state.stats)` | P1 |
| 60 | `DashboardViewModelTest` | `init loads favorites` | `assertTrue(state.favorites.isNotEmpty())` | P1 |
| 61 | `DashboardViewModelTest` | `selectFavoriteTab filters by type` | Correct type filtered | P1 |
| 62 | `DashboardViewModelTest` | `refresh reloads all data` | API called for stats + favorites | P1 |
| 63 | `DashboardViewModelTest` | `concurrent refresh doesn't interleave state` | Final state is consistent | P1 |
| 64 | `DashboardViewModelTest` | `single error field not overwritten by parallel requests` | Error from first failure visible | P1 |
| 65 | `DashboardViewModelTest` | `empty favorites shows empty state` | `assertTrue(state.favorites.isEmpty())` | P2 |
| 66 | `DashboardViewModelTest` | `stats failure shows error` | Error state set | P1 |

## Module: feature:settings

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 67 | `SettingsViewModelTest` | `init loads user profile` | `assertNotNull(state.user)` | P1 |
| 68 | `SettingsViewModelTest` | `saveName calls API with new name` | `coVerify { userRepo.updateProfile(name) }` | P1 |
| 69 | `SettingsViewModelTest` | `saveName with loading guard prevents double-submit` | API called once | P1 |
| 70 | `SettingsViewModelTest` | `signOut clears tokens and Firebase` | `coVerify { authRepo.signOut() }` | P1 |
| 71 | `SettingsViewModelTest` | `signOut with loading guard prevents double-tap` | Called once | P1 |
| 72 | `SettingsViewModelTest` | `clearCache calls cache clear` | Cache cleared | P1 |
| 73 | `SettingsViewModelTest` | `changePassword validates old + new + confirm` | Validation errors set | P1 |
| 74 | `SettingsViewModelTest` | `changePassword inconsistent validation vs AuthVM` | Document discrepancy (length-only vs full) | P2 |
| 75 | `SettingsViewModelTest` | `toggleTts updates DataStore` | Preference updated | P1 |
| 76 | `SettingsViewModelTest` | `ttsSpeed slider updates DataStore` | Preference updated | P1 |

## Module: feature:landing

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 77 | `LandingViewModelTest` | `init loads landing data` | `assertNotNull(state.data)` | P1 |
| 78 | `LandingViewModelTest` | `refresh reloads data` | API called again | P1 |
| 79 | `LandingViewModelTest` | `refresh without double-submit guard is fixed` | Guard works | P1 |
| 80 | `LandingViewModelTest` | `error state on network failure` | Error set | P1 |
| 81 | `LandingViewModelTest` | `concurrent refresh cancels previous job` | Only latest result applies | P1 |
| 82 | `LandingViewModelTest` | `loading state set during API call` | `assertTrue(state.isLoading)` during call | P1 |

## Module: feature:dictionary (untested ViewModels)

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 83 | `SignDetailViewModelTest` | `init loads sign from savedStateHandle` | `assertEquals(signCode, state.sign.code)` | P2 |
| 84 | `SignDetailViewModelTest` | `toggleFavorite with race condition guard` | Only 1 API call on rapid double-tap | P2 |
| 85 | `SignDetailViewModelTest` | `speakSign with loading guard` | Only 1 MediaPlayer at a time | P2 |
| 86 | `SignDetailViewModelTest` | `onCleared releases MediaPlayer properly` | `stop()` then `release()` called | P2 |
| 87 | `SignDetailViewModelTest` | `error loading sign shows error` | Error state set | P2 |
| 88 | `LessonViewModelTest` | `init loads lesson from savedStateHandle level` | `assertEquals(level, state.level)` | P2 |
| 89 | `LessonViewModelTest` | `speakSign with loading guard` | Only 1 player at a time | P2 |
| 90 | `LessonViewModelTest` | `retry with isLoading guard prevents concurrent loads` | Single load | P2 |
| 91 | `LessonViewModelTest` | `TTS failure is silent (no local fallback)` | Verify behavior, document gap | P2 |
| 92 | `TranslateViewModelTest` | `translate with valid input calls API` | `coVerify { translateRepo.translate(text) }` | P2 |
| 93 | `TranslateViewModelTest` | `translate with blank input is no-op` | API not called | P2 |
| 94 | `TranslateViewModelTest` | `translate with isLoading guard` | Single call | P2 |
| 95 | `TranslateViewModelTest` | `error state on failure` | Error set | P2 |
| 96 | `WriteViewModelTest` | `init loads palette` | `assertTrue(state.groups.isNotEmpty())` | P2 |
| 97 | `WriteViewModelTest` | `loadPalette failure shows error (not silent)` | Verify error surfaced | P2 |
| 98 | `WriteViewModelTest` | `compose with valid input calls API` | API called | P2 |
| 99 | `WriteViewModelTest` | `compose with blank input is no-op` | Guard works | P2 |
| 100 | `WriteViewModelTest` | `isPreviewLoading declared but never used` | Document dead code | P2 |

## Module: feature:explore (untested ViewModels)

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 101 | `DetailViewModelTest` | `init loads landmark from savedStateHandle slug` | `assertEquals(slug, state.landmark.slug)` | P2 |
| 102 | `DetailViewModelTest` | `retry with isLoading guard` | Single load | P2 |
| 103 | `DetailViewModelTest` | `toggleFavorite failure shows error` | Error visible to user | P2 |
| 104 | `DetailViewModelTest` | `speak calls audio API` | API called | P2 |
| 105 | `IdentifyViewModelTest` | `onImageCaptured with isLoading guard (double-submit fixed)` | Single API call | P2 |
| 106 | `IdentifyViewModelTest` | `identify success shows results` | Results in state | P2 |
| 107 | `IdentifyViewModelTest` | `identify error shows error state` | Error set | P2 |
| 108 | `IdentifyViewModelTest` | `temp files cleaned on success` | File deleted after upload | P2 |
| 109 | `IdentifyViewModelTest` | `temp files cleaned on error` | File deleted on failure | P2 |

## Module: feature:feedback

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 110 | `FeedbackViewModelTest` | `submit with valid input calls API` | `coVerify { feedbackRepo.submit(any()) }` | P2 |
| 111 | `FeedbackViewModelTest` | `submit with isSubmitting guard` | Single call | P2 |
| 112 | `FeedbackViewModelTest` | `submit with empty category shows validation error` | Error set | P2 |
| 113 | `FeedbackViewModelTest` | `submit with text < 10 chars shows validation error` | Error set | P2 |
| 114 | `FeedbackViewModelTest` | `success state set after submission` | `assertTrue(state.isSuccess)` | P2 |
| 115 | `FeedbackViewModelTest` | `no way to reset form after success` | Document behavior | P2 |

## Module: app (HieroglyphsHub)

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 116 | `HieroglyphsHubViewModelTest` | `init loads random page + suggested signs + dictionary count` | All 3 sections populated | P2 |
| 117 | `HieroglyphsHubViewModelTest` | `random page (1..30) can exceed actual pages` | Verify handling of 404/empty | P2 |
| 118 | `HieroglyphsHubViewModelTest` | `isLoading cleared even if first two coroutines fail` | Loading false after error | P2 |
| 119 | `HieroglyphsHubViewModelTest` | `error state rendered (not silently swallowed)` | Error visible | P2 |
| 120 | `HieroglyphsHubViewModelTest` | `refresh reloads all sections` | API called again | P2 |

## Module: core:common

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 121 | `ConnectivityMonitorTest` | `online flow emits true when connected` | `assertTrue(flow.first())` | P2 |
| 122 | `ConnectivityMonitorTest` | `offline flow emits false when disconnected` | `assertFalse(flow.first())` | P2 |

## Module: core:data (UserPreferencesDataStore)

| # | Test Class | Test Case | Assertion | Priority |
|---|-----------|-----------|-----------|----------|
| 123 | `UserPreferencesDataStoreTest` | `read default ttsEnabled is true` | `assertTrue(prefs.ttsEnabled)` | P2 |
| 124 | `UserPreferencesDataStoreTest` | `write and read ttsSpeed` | `assertEquals(1.5f, prefs.ttsSpeed)` | P2 |
| 125 | `UserPreferencesDataStoreTest` | `write and read language preference` | Correct lang returned | P2 |

---

**Total: 125 new unit test cases across 20 test classes, covering all 17 untested ViewModels + 3 infrastructure classes.**
