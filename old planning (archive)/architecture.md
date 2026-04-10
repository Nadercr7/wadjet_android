# Wadjet Android — Architecture

> MVVM + Clean Architecture + Compose + Hilt + Coroutines/Flow

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────┐
│                    Presentation Layer                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  Composable  │  │  ViewModel  │  │  UI State        │  │
│  │  Screens     │→ │  (per screen)│← │  (data class)    │  │
│  └─────────────┘  └──────┬──────┘  └─────────────────┘  │
│                          │                                │
│                    ┌─────▼─────┐                          │
│                    │ Use Cases │ ← Domain Layer            │
│                    │ (optional)│                           │
│                    └─────┬─────┘                          │
│                          │                                │
├──────────────────────────┼───────────────────────────────┤
│                    Data Layer                              │
│  ┌──────────┐  ┌────────▼─────┐  ┌──────────────────┐   │
│  │ Retrofit  │  │ Repository   │  │ Room Database     │   │
│  │ (API)     │← │ (interface)  │→ │ (local cache)     │   │
│  └──────────┘  └──────────────┘  └──────────────────┘   │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Firebase  │  │ Firestore    │  │ DataStore         │   │
│  │ Auth      │  │ (user data)  │  │ (preferences)     │   │
│  └──────────┘  └──────────────┘  └──────────────────┘   │
│  ┌──────────┐                                            │
│  │ ONNX RT  │ (on-device ML)                             │
│  └──────────┘                                            │
└──────────────────────────────────────────────────────────┘
```

---

## Layer Responsibilities

### 1. Presentation Layer (`:feature:*` modules)

- **Composable Screens**: Jetpack Compose UI, observes ViewModel state
- **ViewModels**: Hold UI state as `StateFlow<UiState>`, handle user events, call use cases/repositories
- **UI State**: Immutable data classes per screen (e.g., `ScanUiState`, `DictionaryUiState`)
- **Navigation**: Compose Navigation with type-safe routes
- **NO business logic here** — ViewModels delegate to repositories/use cases

### 2. Domain Layer (`:core:domain`) — THIN

- **Use Cases**: Only when orchestrating multiple repositories or complex logic
- **Models**: Kotlin data classes (NOT Firestore/Room/Retrofit models)
- **Repository Interfaces**: Defined here, implemented in data layer
- **NO framework dependencies** — pure Kotlin

When to skip use cases: If a ViewModel just calls `repository.getX()` and maps to UI state, skip the use case. Don't add a pass-through layer for the sake of "architecture."

### 3. Data Layer (`:core:data`, `:core:network`, `:core:database`)

- **Repositories**: Implement domain interfaces. Coordinate between API, cache, and Firestore
- **API Services**: Retrofit interfaces for Wadjet backend
- **Room DAOs**: Local cache for offline data
- **Firestore**: User data (auth, favorites, progress, history)
- **DataStore**: User preferences (language, TTS settings)
- **ONNX Runtime**: On-device ML model loading and inference

---

## Module Structure

```
:app                          ← Application entry point, Hilt setup, NavGraph
:core
  :core:designsystem          ← Theme, colors, typography, component library
  :core:domain                ← Repository interfaces, domain models, use cases
  :core:data                  ← Repository implementations, DI modules
  :core:network               ← Retrofit setup, API service interfaces, interceptors
  :core:database              ← Room database, DAOs, entities, type converters
  :core:firebase              ← Firebase Auth, Firestore, FCM wrappers
  :core:ml                    ← ONNX Runtime setup, model loading, inference
  :core:common                ← Utilities, extensions, constants
  :core:ui                    ← Shared composables (LoadingShimmer, ErrorState, etc.)
:feature
  :feature:auth               ← Welcome, Login, Register, Forgot Password screens
  :feature:landing            ← Landing / dual-path hub screen
  :feature:scan               ← Scanner (camera + gallery + results)
  :feature:dictionary         ← Browse + Learn + Write tabs
  :feature:explore            ← Landmarks list + detail + identify
  :feature:chat               ← Thoth AI chatbot
  :feature:stories            ← Story list + reader
  :feature:dashboard          ← User stats, history, favorites, progress
  :feature:settings           ← Profile, language, password, about
  :feature:feedback           ← Feedback form
```

---

## Data Flow Patterns

### Pattern 1: API → Room Cache → UI (Dictionary, Landmarks, Stories)

```kotlin
// Repository
fun getLandmarks(query: LandmarkQuery): Flow<List<Landmark>> = flow {
    // 1. Emit cached data immediately
    val cached = dao.getLandmarks(query)
    if (cached.isNotEmpty()) emit(cached)
    
    // 2. Fetch fresh from API
    try {
        val remote = api.getLandmarks(query.toApiParams())
        dao.insertAll(remote.toEntities())
        emit(dao.getLandmarks(query))
    } catch (e: Exception) {
        if (cached.isEmpty()) throw e  // No cache, propagate error
        // Otherwise, stale cache is fine
    }
}
```

### Pattern 2: Firestore → UI (Favorites, Progress, History)

```kotlin
// Repository
fun getFavorites(userId: String): Flow<List<Favorite>> =
    firestore.collection("users/$userId/favorites")
        .snapshots()
        .map { snapshot -> snapshot.toObjects<FavoriteDto>().toDomain() }
```

### Pattern 3: API Streaming → UI (Chat SSE)

```kotlin
// Repository
fun chatStream(message: String, sessionId: String): Flow<String> = callbackFlow {
    val response = okHttpClient.newCall(request).execute()
    val source = response.body?.source() ?: return@callbackFlow
    
    while (!source.exhausted()) {
        val line = source.readUtf8Line() ?: break
        if (line.startsWith("data: ")) {
            val data = line.removePrefix("data: ")
            if (data == "[DONE]") break
            val chunk = json.decodeFromString<ChatChunk>(data)
            trySend(chunk.text)
        }
    }
    close()
}
```

### Pattern 4: Multipart Upload → API (Scan, Identify)

```kotlin
// Repository
suspend fun scanImage(imageFile: File, mode: String = "auto"): ScanResult {
    val filePart = MultipartBody.Part.createFormData(
        "file", imageFile.name,
        imageFile.asRequestBody("image/jpeg".toMediaType())
    )
    val modePart = mode.toRequestBody("text/plain".toMediaType())
    return api.scan(filePart, modePart).toDomain()
}
```

---

## State Management

### UI State Pattern

```kotlin
// Per-screen sealed UI state
data class ScanUiState(
    val cameraActive: Boolean = true,
    val scanStep: ScanStep = ScanStep.IDLE,
    val result: ScanResult? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
)

enum class ScanStep { IDLE, DETECTING, CLASSIFYING, TRANSLITERATING, TRANSLATING, DONE }

// ViewModel
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    
    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()
    
    fun onImageCaptured(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, scanStep = ScanStep.DETECTING) }
            try {
                val result = scanRepository.scanImage(file)
                _state.update { it.copy(result = result, scanStep = ScanStep.DONE, isLoading = false) }
                historyRepository.saveScan(result)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

### One-Shot Events (Snackbar, Navigation)

```kotlin
// Use SharedFlow for one-shot events
sealed class ScanEvent {
    data class ShowToast(val message: String) : ScanEvent()
    data class NavigateToDetail(val scanId: String) : ScanEvent()
}

private val _events = MutableSharedFlow<ScanEvent>()
val events: SharedFlow<ScanEvent> = _events.asSharedFlow()
```

---

## Network Layer

### Retrofit Setup

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { 
            level = if (BuildConfig.DEBUG) BODY else NONE 
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)  // SSE streams can be long
        .build()
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
}
```

### Auth Interceptor (Token Management)

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    
    private val mutex = Mutex()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getAccessToken()
        val request = chain.request().newBuilder()
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        
        val response = chain.proceed(request)
        
        if (response.code == 401 && token != null) {
            response.close()
            // Synchronized token refresh (single attempt, no race)
            val newToken = runBlocking {
                mutex.withLock {
                    if (tokenManager.getAccessToken() == token) {
                        tokenManager.refreshToken()
                    }
                    tokenManager.getAccessToken()
                }
            }
            val retryRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            return chain.proceed(retryRequest)
        }
        
        return response
    }
}
```

---

## Dependency Injection (Hilt)

### Module Organization

| Module | Scope | Provides |
|--------|-------|----------|
| `NetworkModule` | Singleton | OkHttpClient, Retrofit, API services |
| `DatabaseModule` | Singleton | Room database, DAOs |
| `FirebaseModule` | Singleton | Firebase Auth, Firestore instances |
| `RepositoryModule` | Singleton | All repository implementations |
| `MLModule` | Singleton | ONNX Runtime session, model loaders |
| `DispatcherModule` | Singleton | IO, Default, Main dispatchers (testable) |

---

## Navigation

### NavGraph Structure

```kotlin
sealed class Route {
    @Serializable data object Welcome : Route()
    @Serializable data object Landing : Route()
    @Serializable data object Scan : Route()
    @Serializable data class ScanResult(val scanId: String) : Route()
    @Serializable data object ScanHistory : Route()
    @Serializable data object Dictionary : Route()
    @Serializable data class DictionarySign(val code: String) : Route()
    @Serializable data class Lesson(val level: Int) : Route()
    @Serializable data object Explore : Route()
    @Serializable data class LandmarkDetail(val slug: String) : Route()
    @Serializable data object Identify : Route()
    @Serializable data object Chat : Route()
    @Serializable data class ChatLandmark(val slug: String) : Route()
    @Serializable data object Stories : Route()
    @Serializable data class StoryReader(val storyId: String) : Route()
    @Serializable data object Dashboard : Route()
    @Serializable data object Settings : Route()
    @Serializable data object Feedback : Route()
}
```

### Bottom Nav → Tab Graph

```
BottomNav
├── HomeTab      → Landing → Hieroglyphs features / Landmark features
├── ScanTab      → Scan → ScanResult → ScanHistory
├── ExploreTab   → Explore → LandmarkDetail → ChatLandmark
├── StoriesTab   → Stories → StoryReader
└── ProfileTab   → Dashboard ↔ Settings → Feedback
```

---

## Error Handling Strategy

```kotlin
// Unified Result wrapper
sealed class WadjetResult<out T> {
    data class Success<T>(val data: T) : WadjetResult<T>()
    data class Error(val message: String, val code: Int? = null) : WadjetResult<Nothing>()
    data object Loading : WadjetResult<Nothing>()
}

// API error mapping
fun <T> Response<T>.toWadjetResult(): WadjetResult<T> = when {
    isSuccessful -> WadjetResult.Success(body()!!)
    code() == 401 -> WadjetResult.Error("Session expired", 401)
    code() == 429 -> WadjetResult.Error("Too many requests. Try again later.", 429)
    code() == 500 -> WadjetResult.Error("Server error. Try again.", 500)
    else -> WadjetResult.Error("Something went wrong", code())
}
```

---

## Security Architecture

| Concern | Implementation |
|---------|---------------|
| Token storage | `EncryptedSharedPreferences` (AndroidKeyStore-backed) |
| API keys | `BuildConfig` fields from `local.properties` (not in VCS) |
| Network | HTTPS enforced via `android:usesCleartextTraffic="false"` |
| Firebase rules | Firestore security rules enforce `request.auth.uid == resource.data.userId` |
| Input sanitization | All user input trimmed + length-checked before API calls |
| ProGuard | Obfuscation enabled for release builds |
| Root detection | Optional — log warning but don't block |

---

## Testing Strategy

| Layer | Tool | What to Test |
|-------|------|-------------|
| Unit (ViewModel) | JUnit 5 + Turbine | State transitions, event emissions |
| Unit (Repository) | JUnit 5 + MockK | API → cache coordination, error handling |
| Unit (Use Case) | JUnit 5 | Business logic orchestration |
| Integration (Room) | AndroidJUnit + Room testing | DAO queries, migrations |
| Integration (API) | MockWebServer | Retrofit serialization, error codes |
| UI | Compose Testing | Screen rendering, user interactions |
| E2E | Maestro | Full user flows (auth → scan → results) |
| Screenshot | Paparazzi | Visual regression (light not applicable — dark only) |
