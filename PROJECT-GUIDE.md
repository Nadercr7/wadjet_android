# Wadjet Android — Project Guide / دليل المشروع

> A cheat-sheet for explaining every part of the project.
> ملف مرجعي لشرح كل جزء في المشروع.

---

## Table of Contents
1. [What is Wadjet?](#1-what-is-wadjet)
2. [Architecture](#2-architecture)
3. [Modules](#3-modules)
4. [UI — Jetpack Compose](#4-ui--jetpack-compose)
5. [Navigation](#5-navigation)
6. [Authentication](#6-authentication)
7. [Networking](#7-networking)
8. [Database](#8-database)
9. [State Management](#9-state-management)
10. [Dependency Injection](#10-dependency-injection)
11. [Firebase](#11-firebase)
12. [Design System](#12-design-system)
13. [AI / ML](#13-ai--ml)
14. [Testing](#14-testing)
15. [Build System](#15-build-system)
16. [Features Deep-Dive](#16-features-deep-dive)
17. [Security](#17-security)
18. [Common Interview Q&A](#18-common-interview-qa)

---

## 1. What is Wadjet?

**EN:** Wadjet is an Egyptian heritage app that uses AI to recognize hieroglyphs from photos, explore Egyptian landmarks, chat with an AI Egyptologist (Thoth), read interactive stories about ancient Egypt, and learn to read/write hieroglyphs. The name "Wadjet" comes from the ancient Egyptian cobra goddess of protection.

**AR:** وادجت هو أبليكيشن للتراث المصري بيستخدم AI عشان يتعرف على الهيروغليفي من الصور، وتستكشف المعالم المصرية، وتتكلم مع عالم آثار AI اسمه (تحوت)، وتقرأ قصص تفاعلية عن مصر القديمة، وتتعلم تقرأ وتكتب هيروغليفي. الاسم "وادجت" جاي من إلهة الكوبرا المصرية القديمة اللي بتحمي.

---

## 2. Architecture

### EN:
The app follows **Clean Architecture + MVVM** pattern, split into 18 Gradle modules.

**Clean Architecture** means we split the code into 3 layers:

```
┌─────────────────────────────────────────────┐
│  Presentation Layer  (feature/* modules)    │  ← UI + ViewModels
│  What the user sees and interacts with       │
├─────────────────────────────────────────────┤
│  Domain Layer  (core/domain)                │  ← Business logic
│  Pure Kotlin, no Android dependencies        │  ← Interfaces + Models
├─────────────────────────────────────────────┤
│  Data Layer  (core/data, network, database) │  ← Actual implementations
│  Retrofit, Room, Firebase                    │  ← Where data comes from
└─────────────────────────────────────────────┘
```

**Why?**
- Each layer only knows about the layer below it
- The domain layer has ZERO Android code — it's pure Kotlin
- Feature modules depend on `core/domain` only, NOT on `core/data` or `core/network`
- This makes the code testable, maintainable, and swappable

**MVVM** (Model-View-ViewModel) means:
- **Model** = data classes + repository interfaces (in `core/domain`)
- **View** = Compose screens (in `feature/*`)
- **ViewModel** = holds UI state, handles user actions, calls repositories (in `feature/*`)

The ViewModel survives screen rotations and configuration changes. The View (Compose) just reads state from the ViewModel and shows it.

### AR:
الأبليكيشن بيتبع نمط **Clean Architecture + MVVM** ومقسم لـ 18 module.

**Clean Architecture** معناها إننا بنقسم الكود لـ 3 طبقات:

- **Presentation Layer** (feature/*) ← الشاشات + ViewModels — اللي اليوزر بيشوفه
- **Domain Layer** (core/domain) ← الـ business logic — interfaces و models بس — **مفيهوش أي كود Android خالص** — Kotlin Pure
- **Data Layer** (core/data, network, database) ← التطبيق الفعلي — Retrofit, Room, Firebase

**ليه كده؟**
- كل طبقة بتعرف الطبقة اللي تحتيها بس
- الـ domain layer مفيهاش أي حاجة Android — ده بيخلي الكود سهل الـ testing
- الـ feature modules بتعتمد على `core/domain` بس، مش على `core/data` — ده isolation

**MVVM** معناها:
- **Model** = الداتا كلاسز + الـ repository interfaces
- **View** = شاشات Compose
- **ViewModel** = بيحتفظ بالـ state، بيتعامل مع أكشنز اليوزر، وبينادي الـ repositories

الـ ViewModel بيفضل عايش حتى لو الشاشة اتلفت (rotation) — الـ View (Compose) بتقرأ الـ state وتعرضها بس.

---

## 3. Modules

### EN:
18 modules total: 1 app + 7 core + 10 feature

**Core modules** (shared infrastructure):

| Module | What it does |
|---|---|
| `core/common` | Shared utilities — ToastController, NetworkMonitor, AudioPlaybackManager, Egyptian pronunciation converter |
| `core/domain` | Repository interfaces + domain models — **pure Kotlin, zero Android deps** |
| `core/data` | Repository implementations — bridges network + database + Firebase to domain |
| `core/network` | Retrofit API services, OkHttp interceptors, DTOs, TokenManager, AuthInterceptor |
| `core/database` | Room database, entities, DAOs, migrations |
| `core/firebase` | FirebaseAuthManager, Firebase Messaging |
| `core/designsystem` | Theme, colors, fonts, all reusable UI components (WadjetButton, WadjetCard, etc.) |
| `core/ui` | Shared Compose composition locals (SharedTransitionScope) |
| `core/ml` | ONNX Runtime placeholder for future on-device ML |

**Feature modules** (each feature = 1 module):

| Module | What it does |
|---|---|
| `feature/auth` | Welcome screen + Login/Register/ForgotPassword bottom sheets |
| `feature/landing` | Home dashboard — greeting, usage limits, quick actions |
| `feature/scan` | Upload photo → AI recognizes hieroglyphs → shows annotated result + translations |
| `feature/dictionary` | 3 tabs: Browse signs (FTS search), Learn (lessons), Write (English → hieroglyphs) |
| `feature/explore` | Browse Egyptian landmarks with categories, search, detail pages, Google Maps |
| `feature/chat` | Chat with Thoth AI — SSE streaming, markdown, TTS, STT, message editing |
| `feature/stories` | Interactive stories about ancient Egypt — chapters, AI images, TTS narration |
| `feature/dashboard` | User stats, favorites, scan history, story progress |
| `feature/settings` | Profile edit, TTS settings, password change, sign out |
| `feature/feedback` | Bug report / feedback form |

### AR:
18 module: 1 app + 7 core + 10 feature

**Core modules** (البنية التحتية المشتركة):
- `core/common` — أدوات مشتركة: ToastController، NetworkMonitor، AudioPlaybackManager
- `core/domain` — الـ interfaces والـ models — **Kotlin بيور، مفيش Android خالص**
- `core/data` — تطبيق الـ repositories — بيوصل الـ network والـ database والـ Firebase بالـ domain
- `core/network` — Retrofit services، OkHttp interceptors، TokenManager
- `core/database` — Room database، الجداول، DAOs
- `core/firebase` — Firebase Auth Manager، Messaging
- `core/designsystem` — الثيم، الألوان، الخطوط، الكومبوننتس المشتركة
- `core/ui` — Composition locals مشتركة
- `core/ml` — placeholder لـ ML مستقبلاً

**Feature modules** (كل فيتشر في module لوحده):
- `feature/auth` — شاشة الترحيب + Login/Register/ForgotPassword
- `feature/landing` — الصفحة الرئيسية
- `feature/scan` — ارفع صورة → AI يتعرف على الهيروغليفي
- `feature/dictionary` — قاموس + دروس + كتابة هيروغليفي
- `feature/explore` — استكشاف المعالم المصرية
- `feature/chat` — الشات مع تحوت AI
- `feature/stories` — قصص تفاعلية
- `feature/dashboard` — إحصائيات اليوزر
- `feature/settings` — الإعدادات
- `feature/feedback` — إرسال ملاحظات

---

## 4. UI — Jetpack Compose

### EN:
The entire app is **100% Jetpack Compose** — no XML layouts at all.

**What is Jetpack Compose?**
It's Android's modern toolkit for building UI. Instead of XML layout files, you write UI as Kotlin functions annotated with `@Composable`.

```kotlin
// Old way (XML): activity_main.xml + MainActivity.kt
// Our way (Compose): everything in Kotlin

@Composable
fun ScanScreen(viewModel: ScanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column {
        Text("Scan a Hieroglyph")
        Button(onClick = { viewModel.pickImage() }) {
            Text("Choose Image")
        }
    }
}
```

**Key Compose features we use:**

| Feature | What it means |
|---|---|
| **Material 3** | Google's latest design system — we use its components (Button, Card, TopAppBar, BottomSheet, etc.) |
| **Compose BOM** | Bill of Materials — one version number controls all Compose library versions (2026.03.00) |
| **NavigationSuiteScaffold** | Adaptive navigation — automatically shows bottom bar on phones, navigation rail on tablets |
| **Shared Element Transitions** | Smooth animations when transitioning between screens (e.g., image in list → full screen) |
| **collectAsStateWithLifecycle()** | Safely converts StateFlow to Compose state — stops collecting when screen is in background |
| **LazyColumn / LazyRow** | Efficient scrollable lists — only renders visible items (like RecyclerView but in Compose) |
| **HorizontalPager** | Swipeable page carousel (used in landmark image galleries) |
| **Scaffold** | Standard Material layout template with TopBar, BottomBar, FAB, content area |

### AR:
الأبليكيشن كله **100% Jetpack Compose** — مفيش XML خالص.

**يعني إيه Compose؟**
بدل ما تكتب الـ UI في ملفات XML وتربطها بالكود، بتكتب كل حاجة ككود Kotlin عادي في functions عليها `@Composable`.

**أهم الحاجات اللي بنستخدمها:**
- **Material 3** — أحدث design system من Google — بنستخدم الكومبوننتس بتاعته (Button, Card, TopAppBar, BottomSheet)
- **Compose BOM** — رقم version واحد بيتحكم في كل مكتبات Compose
- **NavigationSuiteScaffold** — بيعمل bottom bar على الموبايل وnavigation rail على التابلت أوتوماتيك
- **Shared Element Transitions** — أنيميشنز smooth بين الشاشات
- **collectAsStateWithLifecycle()** — بتحول الـ StateFlow لـ Compose state بأمان — بتوقف الـ collection لما الشاشة في الـ background
- **LazyColumn / LazyRow** — lists بتعمل render للعناصر الظاهرة بس (زي RecyclerView بس في Compose)

---

## 5. Navigation

### EN:
We use **Type-safe Compose Navigation 2.9.7** — the latest approach where routes are defined as Kotlin `@Serializable` classes/objects.

```kotlin
// Route definitions (in Route.kt):
@Serializable object Splash
@Serializable object Welcome
@Serializable object Landing
@Serializable data class ScanResult(val scanId: String)
@Serializable data class LandmarkDetail(val slug: String)
@Serializable data class ChatLandmark(val slug: String)
@Serializable data class StoryReader(val storyId: String)
```

**Why type-safe?** The old way used string routes like `"scan_result/{scanId}"` which could have typos. Now the compiler checks everything.

**5 bottom navigation tabs:**
1. 🏠 Home → `Route.Landing`
2. 𓀀 Hieroglyphs → `Route.Hieroglyphs` (hub for Scan + Dictionary + Write)
3. 🏛 Explore → `Route.Explore`
4. 📖 Stories → `Route.Stories`
5. 🗣 Thoth → `Route.Chat`

**How navigation works:**
- `navController.navigate(Route.ScanResult(scanId = "abc"))` — navigate with type-safe params
- `savedStateHandle.toRoute<Route.ScanResult>()` — ViewModel reads params from SavedStateHandle
- Tab switches use `popUpTo(startDestination) { saveState = true }` + `restoreState = true` — this preserves state when switching tabs

### AR:
بنستخدم **Type-safe Compose Navigation 2.9.7** — أحدث طريقة، الـ routes بتتعرف كـ Kotlin classes عليها `@Serializable`.

**ليه type-safe؟** الطريقة القديمة كانت بتستخدم strings زي `"scan_result/{scanId}"` ودي ممكن يحصل فيها typo ومش هتعرف غير وقت الـ runtime. دلوقتي الـ compiler بيتشيك كل حاجة.

**5 tabs في الـ bottom bar:**
1. Home — الصفحة الرئيسية
2. Hieroglyphs — مركز الهيروغليفي (Scan + Dictionary + Write)
3. Explore — استكشاف المعالم
4. Stories — القصص
5. Thoth — الشات مع AI

**إزاي بتشتغل:**
- `navController.navigate(Route.ScanResult(scanId = "abc"))` — الـ navigation مع params بشكل type-safe
- `savedStateHandle.toRoute<Route.ScanResult>()` — الـ ViewModel بيقرأ الـ params
- لما بتنقل بين الـ tabs بيحتفظ بالـ state بتاع كل tab — يعني لو كنت نازل في list وروحت tab تاني ورجعت، هتلاقيها زي ما هي

---

## 6. Authentication

### EN:
We use a **dual-layer authentication** system:

```
┌──────────────────────────────────────────────────────┐
│  Layer 1: Firebase Auth (Identity)                    │
│  → User signs in with Google or Email/Password        │
│  → Firebase gives us an ID Token                      │
├──────────────────────────────────────────────────────┤
│  Layer 2: Custom Backend JWT (API Access)             │
│  → App sends Firebase ID Token to our FastAPI backend │
│  → Backend verifies it and returns access_token +     │
│    refresh_token (our own JWTs)                       │
│  → These tokens are used for all API calls            │
└──────────────────────────────────────────────────────┘
```

**Why not just Firebase Auth?**
Because our backend is a custom FastAPI server (not Firebase Functions). We need our own tokens to control access, rate limiting, and user data on our server.

**Auth providers:**
1. **Google Sign-In** — via `Credential Manager API` (modern replacement for old Google Sign-In SDK)
2. **Email/Password** — classic Firebase Auth

**Token storage:**
- Stored in **EncryptedSharedPreferences** (uses AES256 encryption)
- Regular SharedPreferences stores tokens in plain text — anyone with root access can read them
- EncryptedSharedPreferences encrypts both keys and values

**Auto-refresh flow:**
1. API call returns 401 (Unauthorized)
2. **TokenAuthenticator** (OkHttp `Authenticator`) intercepts it
3. Calls `POST /api/auth/refresh` with the refresh token
4. Gets new access token → retries the original request
5. If refresh also fails → `TokenManager.invalidateSession()` → signs out the user

**AuthInterceptor:**
- Adds `Authorization: Bearer <token>` header to every API request
- BUT only for our backend URL — skips external URLs (like Wikipedia image CDN)

### AR:
بنستخدم **نظام أوثنتكيشن مزدوج**:

**الطبقة الأولى: Firebase Auth (الهوية)**
- اليوزر بيسجل دخول بـ Google أو Email/Password
- Firebase بيدينا ID Token

**الطبقة التانية: Custom JWT من الـ backend (للـ API)**
- الأبليكيشن بيبعت الـ Firebase ID Token للـ backend بتاعنا (FastAPI)
- الـ backend بيتأكد منه وبيرجع access_token + refresh_token (JWTs خاصة بينا)
- التوكنز دي بنستخدمها في كل الـ API calls

**ليه مش Firebase Auth بس؟**
عشان الـ backend بتاعنا custom (FastAPI)، مش Firebase Functions — محتاجين tokens خاصة بينا عشان نتحكم في الـ access والـ rate limiting.

**الـ providers:**
1. **Google Sign-In** — عن طريق Credential Manager API (البديل الحديث للـ Google Sign-In SDK القديم)
2. **Email/Password** — Firebase Auth عادي

**تخزين التوكنز:**
- مخزنة في **EncryptedSharedPreferences** (تشفير AES256)
- الـ SharedPreferences العادية بتخزن في plain text — أي حد عنده root ممكن يقراهم
- EncryptedSharedPreferences بتشفر الـ keys والـ values

**الـ auto-refresh:**
1. API call بيرجع 401
2. **TokenAuthenticator** بيمسك الـ response
3. بيعمل `POST /api/auth/refresh` بالـ refresh token
4. بياخد access token جديد → بيعيد الـ request الأصلي
5. لو الـ refresh فشل → sign out تلقائي

**AuthInterceptor:**
- بيضيف `Authorization: Bearer <token>` على كل request
- بس على الـ backend URL بتاعنا بس — بيتخطى URLs خارجية (زي Wikipedia)

---

## 7. Networking

### EN:
**Stack: Retrofit 2.11.0 + OkHttp 4.12.0 + kotlinx.serialization**

**Retrofit** is a type-safe HTTP client for Android. You define API calls as Kotlin interfaces:
```kotlin
interface ScanApiService {
    @Multipart
    @POST("api/scan")
    suspend fun scan(
        @Part image: MultipartBody.Part,
        @Query("mode") mode: String = "auto"
    ): ScanResponse
}
```

**OkHttp** is the HTTP engine under Retrofit. We add interceptors (middleware):
1. **AuthInterceptor** — adds Bearer token to requests
2. **RateLimitInterceptor** — handles 429 (Too Many Requests) responses
3. **HttpLoggingInterceptor** — logs requests/responses in debug builds (NONE in release)
4. **TokenAuthenticator** — handles 401 by refreshing the token

**kotlinx.serialization** (not Gson, not Moshi):
- Kotlin-first JSON serializer — uses `@Serializable` annotation on data classes
- More performant and type-safe than Gson
- We use `retrofit2-kotlinx-serialization-converter` to connect it with Retrofit

**Chat Streaming (SSE):**
The chat doesn't use normal REST. It uses **Server-Sent Events (SSE)** — the server sends tokens one-by-one in real-time:
- Uses raw OkHttp (not Retrofit) + `okhttp-sse` library
- Wrapped in Kotlin `callbackFlow` for reactive streaming
- Each token arrives → updates UI → user sees text appearing word-by-word (like ChatGPT)

**Backend:** FastAPI server hosted on Hugging Face Space (`https://nadercr7-wadjet-v2.hf.space`)

### AR:
**المكونات: Retrofit 2.11.0 + OkHttp 4.12.0 + kotlinx.serialization**

**Retrofit** هو HTTP client type-safe لأندرويد. بتعرف الـ API calls كـ Kotlin interfaces.

**OkHttp** هو الـ engine اللي تحت Retrofit. بنضيف عليه interceptors (middleware):
1. **AuthInterceptor** — بيضيف الـ Bearer token
2. **RateLimitInterceptor** — بيتعامل مع 429 (كتر requests)
3. **HttpLoggingInterceptor** — بيعمل log للـ requests في الـ debug (متعطل في الـ release)
4. **TokenAuthenticator** — بيتعامل مع 401 ويعمل refresh

**kotlinx.serialization** (مش Gson ولا Moshi):
- Kotlin-first JSON serializer — بيستخدم `@Serializable` على الـ data classes
- أسرع وأكتر type-safety من Gson
- بنستخدم converter عشان يشتغل مع Retrofit

**الشات (SSE):**
الشات مش REST عادي — بيستخدم **Server-Sent Events** — السيرفر بيبعت الكلام token بـ token في real-time:
- بنستخدم OkHttp مباشرة (مش Retrofit) + مكتبة `okhttp-sse`
- ملفوف في `callbackFlow` (Kotlin Coroutines)
- كل token بيوصل → الـ UI بيتحدث → اليوزر بيشوف الكلام بيظهر كلمة كلمة (زي ChatGPT)

---

## 8. Database

### EN:
**Room 2.7.1** — Android's official SQLite wrapper.

Room has 3 main components:
1. **Entity** — a data class that maps to a database table
2. **DAO** (Data Access Object) — an interface with SQL queries
3. **Database** — the database class that ties everything together

```kotlin
// Entity example:
@Entity(tableName = "signs")
data class SignEntity(
    @PrimaryKey val code: String,      // "A1", "G17"
    val glyph: String,                 // "𓀀", "𓅓"
    val transliteration: String?,
    val description: String?,
    val category: String?
)

// DAO example:
@Dao
interface SignDao {
    @Query("SELECT * FROM signs WHERE code = :code")
    suspend fun getByCode(code: String): SignEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signs: List<SignEntity>)
}
```

**Our tables:**

| Table | Purpose |
|---|---|
| `signs` + `signs_fts` | Hieroglyphic dictionary with **FTS4** (Full-Text Search — lets users search by code, glyph, meaning, etc.) |
| `scan_results` | Scan history — stores thumbnail + serialized JSON result |
| `landmarks` | Cached landmarks for offline browsing |
| `categories` | Landmark categories cache |
| `story_progress` | Local reading progress per story |
| `favorites` | User favorites with type column (landmark/glyph/story) |

**Schema version:** 7 (with migrations for upgrades)

**FTS4** (Full-Text Search): A special virtual table that enables fast text search across multiple columns. We can search for "cat" and find signs where the description mentions cats.

### AR:
**Room 2.7.1** — الـ SQLite wrapper الرسمي من Android.

Room عنده 3 مكونات:
1. **Entity** — data class بتمثل جدول في الداتابيز
2. **DAO** — interface فيها الـ SQL queries
3. **Database** — الكلاس اللي بيربط كل حاجة

**الجداول بتاعتنا:**
- `signs` + `signs_fts` — قاموس الهيروغليفي مع **FTS4** (بحث نصي سريع — اليوزر يقدر يدور بالكود أو المعنى أو الوصف)
- `scan_results` — تاريخ الـ scans
- `landmarks` — المعالم مخزنة locally عشان تشتغل offline
- `categories` — أنواع المعالم
- `story_progress` — تقدم القراءة في القصص
- `favorites` — المفضلة (معالم/رموز/قصص)

**FTS4** = Full-Text Search — جدول virtual بيمكن البحث السريع في أكتر من column. يعني لو اليوزر كتب "cat" هيلاقي كل الرموز اللي وصفها فيه "cat".

---

## 9. State Management

### EN:
We use **StateFlow + SharedFlow** exclusively. **No LiveData.**

```kotlin
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    // UI State — holds everything the screen needs to display
    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    // One-shot events — navigation, toasts (things that happen once)
    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()
    
    fun onScanClicked(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = scanRepository.scan(bitmap, "auto")
            _state.update { it.copy(isLoading = false, result = result) }
        }
    }
}
```

**StateFlow vs LiveData — why StateFlow?**
- StateFlow is Kotlin-native (works without Android dependencies)
- Has an initial value (no null issues)
- Better for Compose — `collectAsStateWithLifecycle()` auto-stops when app is in background
- Works perfectly with coroutines

**SharedFlow vs StateFlow:**
- **StateFlow** = always has a current value, UI reads it continuously (like "is loading?", "what's the scan result?")
- **SharedFlow** = fire-and-forget events (like "navigate to next screen", "show toast") — emitted once, consumed once

**ToastController:**
- A `@Singleton` class in `core/common`
- Uses `Channel<ToastEvent>(BUFFERED)` — Channel guarantees delivery (unlike SharedFlow which can lose events)
- `MainActivity` collects from it → shows `WadjetToast` composable overlay
- Any ViewModel can inject `ToastController` and call `showToast("message")`

**DataStore Preferences:**
- Used for user settings (TTS config, preferences)
- Modern replacement for SharedPreferences for non-sensitive data
- Async + coroutine-based (SharedPreferences is synchronous and can block the main thread)

### AR:
بنستخدم **StateFlow + SharedFlow** بس. **مفيش LiveData.**

**StateFlow vs LiveData — ليه StateFlow؟**
- StateFlow من Kotlin نفسها (مش محتاجة Android)
- عندها initial value (مفيش مشاكل null)
- أحسن مع Compose — `collectAsStateWithLifecycle()` بتوقف أوتوماتيك لما الأبليكيشن في الـ background
- بتشتغل مع coroutines بشكل ممتاز

**الفرق بين StateFlow و SharedFlow:**
- **StateFlow** = عندها قيمة حالية دايماً، الـ UI بيقراها باستمرار (زي "بيحمل؟"، "إيه نتيجة الـ scan؟")
- **SharedFlow** = أحداث بتتبعت مرة وبتتاكل مرة (زي "روح الشاشة التانية"، "اعرض toast")

**ToastController:**
- `@Singleton` في `core/common`
- بيستخدم `Channel` — بيضمن التوصيل
- `MainActivity` بتاخد منه → بتعرض `WadjetToast`
- أي ViewModel يقدر يعمل inject لـ ToastController ويعرض toast

---

## 10. Dependency Injection

### EN:
**Hilt 2.53.1** — built on top of Dagger, simplified for Android.

**What is Dependency Injection (DI)?**
Instead of creating objects yourself, you tell the framework what you need and it creates + provides them.

```kotlin
// WITHOUT DI (bad):
class ScanViewModel {
    val repository = ScanRepositoryImpl(
        ScanApiService(Retrofit.create(...)),
        ScanResultDao(Room.databaseBuilder(...).build())
    )  // 😱 You have to create everything manually
}

// WITH Hilt (good):
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository  // Hilt provides this automatically
) : ViewModel()
```

**Key annotations:**
- `@HiltAndroidApp` on `WadjetApplication` — enables Hilt for the whole app
- `@AndroidEntryPoint` on `MainActivity` — enables injection in this Activity
- `@HiltViewModel` on ViewModels — enables constructor injection
- `@Inject constructor(...)` — tells Hilt "create this by injecting these params"
- `@Module` + `@InstallIn(SingletonComponent::class)` — defines how to create objects
- `@Provides` — "here's how to create this object"
- `@Binds` — "when someone asks for this interface, give them this implementation"
- `@Singleton` — only create one instance, reuse it everywhere
- `@Named("baseUrl")` — when you have multiple objects of the same type

**We use KSP (not KAPT)** for annotation processing — KSP is 2x faster than KAPT.

**Our DI modules:**
- `NetworkModule` — creates OkHttpClient, Retrofit, all API services
- `DatabaseModule` — creates Room database, all DAOs
- `RepositoryModule` — binds repository interfaces to implementations
- `FirebaseModule` — creates FirebaseAuth, Firestore
- `DispatchersModule` — provides coroutine dispatchers

### AR:
**Hilt 2.53.1** — مبني على Dagger، متبسط لأندرويد.

**يعني إيه Dependency Injection؟**
بدل ما تعمل الـ objects بنفسك، بتقول للـ framework "أنا محتاج الحاجة دي" وهو بيعملها ويديهالك.

**أهم الـ annotations:**
- `@HiltAndroidApp` على `WadjetApplication` — بيفعّل Hilt للأبليكيشن كله
- `@AndroidEntryPoint` على `MainActivity` — بيمكّن الـ injection في الـ Activity دي
- `@HiltViewModel` على الـ ViewModels — بيمكّن الـ constructor injection
- `@Inject constructor(...)` — بيقول لـ Hilt "اعملي الحاجات دي وادّيهالي"
- `@Module` — بيعرّف إزاي يتعمل كل object
- `@Provides` — "اعمل الـ object ده كده"
- `@Binds` — "لما حد يطلب الـ interface ده، ادّيه الـ implementation ده"
- `@Singleton` — اعمل instance واحدة بس واستخدمها في كل حتة

**بنستخدم KSP (مش KAPT)** — KSP أسرع مرتين من KAPT في الـ annotation processing.

---

## 11. Firebase

### EN:

| Service | What we use it for |
|---|---|
| **Firebase Auth** | User identity — email/password + Google Sign-In. We exchange the Firebase ID token for our own backend JWT |
| **Cloud Firestore** | Story progress real-time sync across devices |
| **Firebase Cloud Messaging (FCM)** | Push notifications |
| **Firebase Analytics** | Usage tracking (which features are used most, etc.) |
| **Firebase Crashlytics** | Automatic crash reporting in release builds |

**Important:** Firebase Auth is only for identity. Our actual API auth uses custom JWTs from our FastAPI backend.

### AR:

| الخدمة | بنستخدمها في إيه |
|---|---|
| **Firebase Auth** | هوية اليوزر — email/password + Google. بنبدل الـ Firebase token بـ JWT خاص بينا |
| **Cloud Firestore** | sync تقدم القصص بين الأجهزة |
| **FCM** | Push notifications |
| **Analytics** | تتبع الاستخدام |
| **Crashlytics** | تقارير الـ crashes في الـ release |

**مهم:** Firebase Auth بس للهوية. الـ API auth الفعلي بيستخدم JWTs خاصة بينا من الـ FastAPI backend.

---

## 12. Design System

### EN:
We built a custom design system in `core/designsystem` — dark-only theme with Egyptian aesthetic.

**Colors (WadjetColors):**
- **Gold** `#D4AF37` — primary color, CTAs (inspired by Egyptian gold)
- **Night** `#0A0A0A` — background
- **Surface** `#141414` — card backgrounds
- **Ivory** `#F5F0E8` — primary text
- **Sand** `#C4A265` — secondary text
- **Lapis** `#26648B` — intermediate difficulty (inspired by lapis lazuli stone)
- **Carnelian** `#A63A28` — advanced difficulty (inspired by carnelian gemstone)

**Typography (fonts):**
- **Playfair Display** — serif display headings (elegant, editorial)
- **Inter** — body text (clean, readable)
- **JetBrains Mono** — monospace for MdC transliteration codes
- **Noto Sans Egyptian Hieroglyphs** — renders actual hieroglyph Unicode characters (𓀀𓁀𓂀)
- **Cairo** — Arabic text support

Typography auto-switches font family based on device locale (Cairo for Arabic, Playfair + Inter for English).

**Custom components:**
`WadjetButton`, `WadjetCard`, `WadjetTopBar`, `WadjetTextField`, `WadjetSearchBar`, `WadjetToast`, `WadjetBadge`, `WadjetAsyncImage`, `TtsButton`, `ShimmerPlaceholders`, `ShimmerEffect`, `StreamingDots`, `EmptyState`, `OfflineIndicator`

**Dark-only:** No light mode. The Egyptian gold-on-black aesthetic is core to the brand.

### AR:
بنينا design system خاص في `core/designsystem` — ثيم dark بس بأسلوب مصري قديم.

**الألوان:**
- **دهبي** `#D4AF37` — اللون الأساسي (مستوحى من الدهب المصري)
- **أسود** `#0A0A0A` — الخلفية
- **عاجي** `#F5F0E8` — النص الأساسي
- **رملي** `#C4A265` — النص الثانوي
- **لازورد** `#26648B` — المستوى المتوسط (مستوحى من حجر اللازورد)
- **عقيق** `#A63A28` — المستوى المتقدم (مستوحى من حجر العقيق الأحمر)

**الخطوط:**
- **Playfair Display** — العناوين (serif أنيق)
- **Inter** — النص العادي
- **JetBrains Mono** — أكواد الهيروغليفي
- **Noto Sans Egyptian Hieroglyphs** — بيعرض رموز الهيروغليفي الحقيقية (𓀀𓁀𓂀)
- **Cairo** — للنص العربي

**Dark بس** — مفيش light mode. الأسلوب المصري (دهبي على أسود) أساسي في الـ brand.

---

## 13. AI / ML

### EN:
All AI runs on the **backend server** (FastAPI on Hugging Face Space) — the Android app just sends data and receives results.

**Scan (Hieroglyph Recognition):**
1. User picks a photo
2. App sends it to `POST /api/scan?mode=auto` (multipart upload)
3. Backend runs a 4-level fallback: ONNX classifier → Gemini AI → Grok AI → ensemble
4. Returns: annotated image, glyph list, transliteration, translations (EN + AR), confidence, AI notes

**Identify (Landmark Recognition):**
1. User takes/picks a photo of a landmark
2. App sends it to `POST /api/explore/identify`
3. Backend runs ONNX + Gemini ensemble
4. Returns: top-3 matching landmarks with confidence + source + agreement badge

**TTS (Text-to-Speech):**
- Primary: `POST /api/audio/speak` → Gemini TTS (voices: Orus for Thoth, Aoede for stories, Charon for landing)
- Fallback: if server returns 204 → app converts MdC transliteration to pronounceable English using `EgyptianPronunciation` utility → uses Android's built-in TextToSpeech engine

**STT (Speech-to-Text):**
- Primary: `POST /api/audio/stt` → Groq Whisper (fast, free)
- Fallback: Android's built-in SpeechRecognizer

**`core/ml` module:**
- Currently a placeholder with ONNX Runtime 1.20.0 dependency
- Prepared for future on-device inference (offline hieroglyph recognition)

### AR:
كل الـ AI بيشتغل على **الـ backend** (FastAPI على Hugging Face Space) — الأبليكيشن بيبعت الداتا وبيستقبل النتايج بس.

**الـ Scan (التعرف على الهيروغليفي):**
1. اليوزر بيختار صورة
2. الأبليكيشن بيبعتها لـ `POST /api/scan`
3. الـ backend بيشغّل 4 مستويات: ONNX → Gemini → Grok → ensemble
4. بيرجع: صورة annotated، قائمة رموز، ترجمة حرفية، ترجمة (إنجليزي + عربي)، ثقة، ملاحظات AI

**TTS (تحويل النص لكلام):**
- أساسي: Gemini TTS من السيرفر (3 أصوات مختلفة حسب السياق)
- Fallback: لو السيرفر رجع 204 → الأبليكيشن بيحول الـ transliteration لإنجليزي منطوق → بيستخدم TextToSpeech بتاع Android

**STT (تحويل الكلام لنص):**
- أساسي: Groq Whisper من السيرفر
- Fallback: SpeechRecognizer بتاع Android

**`core/ml`:**
- حالياً placeholder — محضّر لـ ML على الجهاز مستقبلاً (تعرف offline)

---

## 14. Testing

### EN:

| Framework | What it does |
|---|---|
| **JUnit 4** | Unit test runner |
| **MockK 1.13.13** | Kotlin-native mocking library — mock objects, verify calls |
| **Turbine 1.2.0** | Test StateFlow/SharedFlow — `flow.test { awaitItem() }` |
| **kotlinx-coroutines-test** | `runTest {}` for testing suspend functions |
| **MockWebServer** | Fake HTTP server for testing network code |
| **Room in-memory DB** | Test DAOs without a real database |
| **Espresso + Compose UI Test** | Instrumented UI tests |

**What we test:**
- All 9 repository implementations (unit tests with mocked API services + DAOs)
- OkHttp interceptors (AuthInterceptor, RateLimitInterceptor, TokenAuthenticator)
- Egyptian pronunciation utility
- Room DAOs (instrumented tests with in-memory database)

**Example test pattern:**
```kotlin
@Test
fun `scan returns success when API responds`() = runTest {
    coEvery { scanApi.scan(any(), any()) } returns mockScanResponse
    
    val result = repository.scan(bitmap, "auto")
    
    result.test {
        assertThat(awaitItem()).isInstanceOf(WadjetResult.Success::class.java)
    }
}
```

### AR:

| المكتبة | بتعمل إيه |
|---|---|
| **JUnit 4** | بيشغّل الـ unit tests |
| **MockK** | بتعمل mock objects — تقدر تتحكم في إيه اللي بيترجع |
| **Turbine** | بتختبر StateFlow/SharedFlow |
| **coroutines-test** | `runTest {}` لاختبار suspend functions |
| **MockWebServer** | سيرفر HTTP مزيف لاختبار كود الـ network |
| **Room in-memory** | داتابيز في الـ RAM بس لاختبار DAOs |

**بنختبر إيه:**
- كل الـ repository implementations
- الـ interceptors (Auth, RateLimit, Token refresh)
- الـ pronunciation utility
- الـ Room DAOs

---

## 15. Build System

### EN:

| Setting | Value |
|---|---|
| **Kotlin** | 2.1.0 |
| **AGP** (Android Gradle Plugin) | 8.7.3 |
| **Gradle** | ~8.x |
| **compileSdk** | 35 (Android 15) |
| **targetSdk** | 35 |
| **minSdk** | 26 (Android 8.0 Oreo) |
| **JVM target** | 17 |
| **Application ID** | `com.wadjet.app` |
| **Version** | 1.0.0 (versionCode 1) |

**Build types:**
- **debug** — uses `BASE_URL` from `local.properties`, logging enabled
- **release** — production URL hardcoded, R8 minification + shrinkResources enabled, ProGuard rules, signed with keystore

**Plugins used:** `android-application`, `android-library`, `kotlin-android`, `kotlin-compose`, `kotlin-serialization`, `hilt-android`, `ksp`, `google-services`, `firebase-crashlytics`

**Compose Compiler Config:** stability configuration file at root (`compose_compiler_config.conf`) to mark certain classes as stable for Compose performance.

### AR:

| الإعداد | القيمة |
|---|---|
| **Kotlin** | 2.1.0 |
| **compileSdk** | 35 (Android 15) |
| **minSdk** | 26 (Android 8.0) |
| **JVM** | 17 |
| **Application ID** | `com.wadjet.app` |

**أنواع الـ build:**
- **debug** — URL من `local.properties`، الـ logging شغال
- **release** — URL الإنتاج، R8 minification + shrinkResources، توقيع بـ keystore

---

## 16. Features Deep-Dive

### Scan (التعرف على الهيروغليفي)

**EN:**
1. User opens Scan screen → picks image from gallery (CameraX code exists but disabled)
2. `ScanViewModel.onImageSelected()` → saves temp file → calls `ScanRepository.scan(bitmap, "auto")`
3. Multipart `POST /api/scan` → backend processes → returns `ScanResponse`
4. `ScanResultScreen` shows: annotated image, glyph chips (`LazyRow`), transliteration + TTS button, translations (EN/AR)
5. Result saved to Room (ScanResultEntity) for scan history
6. Animated progress: `ScanStep` enum (IDLE → DETECTING → CLASSIFYING → TRANSLITERATING → TRANSLATING → DONE)

**AR:**
1. اليوزر بيفتح Scan → بيختار صورة من الجاليري
2. الـ ViewModel بيحفظ ملف مؤقت → بينادي الـ repository
3. الصورة بتتبعت للـ backend → بيرجع `ScanResponse`
4. شاشة النتيجة بتعرض: صورة annotated، شريط رموز، ترجمة حرفية + زرار TTS، ترجمة إنجليزي وعربي
5. النتيجة بتتحفظ في Room عشان الـ history
6. أنيميشن progress بتتبع خطوات (كشف → تصنيف → ترجمة حرفية → ترجمة → خلاص)

### Chat (الشات مع تحوت AI)

**EN:**
- SSE streaming via raw OkHttp `EventSource` — token-by-token
- Wrapped in `callbackFlow` for Compose consumption
- Session managed by UUID, stored as JSON files in `filesDir/chat_history/`
- Markdown rendering via `compose-markdown` library
- TTS per message (speaker icon or long-press)
- STT (mic button → speech to text)
- Message edit: long-press "Edit" → text fills input → history truncated at that point → resend (ChatGPT-style)
- Landmark mode: `Route.ChatLandmark(slug)` opens chat with landmark context pre-loaded

**AR:**
- بيستخدم SSE — الكلام بييجي token بـ token (زي ChatGPT)
- Session بـ UUID، متحفظ كـ JSON files
- Markdown rendering — الرسائل بتتعرض formatted
- TTS لكل رسالة (أيقونة السماعة أو long-press)
- STT (زرار المايك → كلام لنص)
- تعديل الرسالة: long-press "Edit" → النص بيملا الـ input → الـ history بيتقطع عند النقطة دي → بيتبعت تاني
- وضع المعالم: بيفتح الشات مع سياق معلم معين محمّل مسبقاً

### Dictionary (القاموس)

**EN:** 3 tabs:
1. **Browse** — FTS4 local search + API search; sign cards in `LazyColumn`; tap → sign detail
2. **Learn** — Egyptian alphabet + structured lessons by level
3. **Write** — Type English → `POST /api/write?mode=smart` → AI converts to hieroglyphs → shows glyph sequence + breakdown

**AR:** 3 tabs:
1. **تصفح** — بحث FTS4 محلي + API; بطاقات رموز; اضغط → تفاصيل الرمز
2. **تعلم** — الأبجدية المصرية + دروس مرتبة حسب المستوى
3. **كتابة** — اكتب إنجليزي → AI يحوله لهيروغليفي → بيعرض تسلسل الرموز

### Stories (القصص)

**EN:**
- List filtered by difficulty (Beginner/Intermediate/Advanced)
- `StoryReaderScreen`: chapter-by-chapter, scene images (AI-generated on-demand), TTS narration
- Progress saved locally (Room) + synced (Firestore)

**AR:**
- قائمة مفلترة حسب الصعوبة (مبتدئ/متوسط/متقدم)
- قارئ القصص: فصل بفصل، صور المشاهد (AI بيولّدها عند الطلب)، سرد صوتي TTS
- التقدم متحفظ محلياً (Room) + متزامن (Firestore)

### Explore (الاستكشاف)

**EN:**
- Paginated landmark grid with category filter + city filter + search (400ms debounce)
- Landmarks cached in Room for offline
- Detail: image carousel (HorizontalPager), sections, Google Maps, "Ask Thoth" button, recommendations
- Identify: upload photo → ensemble AI → top-3 matches

**AR:**
- شبكة معالم مع فلتر أنواع + مدن + بحث
- المعالم متخزنة في Room عشان تشتغل offline
- التفاصيل: كاروسيل صور، أقسام، Google Maps، زرار "اسأل تحوت"، توصيات
- التعرف: ارفع صورة → AI → أفضل 3 نتايج

---

## 17. Security

### EN:
1. **EncryptedSharedPreferences** — tokens encrypted with AES256 (keys: AES256-SIV, values: AES256-GCM)
2. **AuthInterceptor scoped** — Bearer token only sent to our backend URL, NOT to external URLs (Wikipedia, etc.)
3. **R8/ProGuard** — code obfuscation in release builds
4. **No secrets in code** — API keys and keystore passwords come from `local.properties` (gitignored) or environment variables
5. **Token auto-refresh** — expired tokens refreshed transparently; if refresh fails → full sign-out
6. **HttpLoggingInterceptor** — logs request/response BODY in debug, NONE in release (prevents token leaks in production logs)

### AR:
1. **EncryptedSharedPreferences** — التوكنز متشفرة بـ AES256
2. **AuthInterceptor scoped** — الـ Bearer token بيتبعت للـ backend بتاعنا بس، مش URLs خارجية
3. **R8/ProGuard** — تشفير الكود في الـ release
4. **مفيش secrets في الكود** — API keys والـ keystore passwords بتيجي من `local.properties` (مش في Git)
5. **Token auto-refresh** — التوكنز المنتهية بتتجدد أوتوماتيك
6. **Logging** — بيسجل كل حاجة في الـ debug، مفيش تسجيل في الـ release (عشان متسربش tokens)

---

## 18. Common Interview Q&A

### Architecture & Design

**Q: What architecture do you use?**
Clean Architecture + MVVM with 18 Gradle modules (7 core + 10 feature + 1 app). Domain layer is pure Kotlin with zero Android dependencies.

**Q: Why multi-module?**
- Build speed: only changed modules recompile
- Separation of concerns: each feature is independent
- Enforced boundaries: feature modules can't access each other directly
- Team scalability: different devs can work on different modules

**Q: Why Clean Architecture?**
- Testability: domain layer has no Android deps, easy to unit test
- Flexibility: can swap data sources without touching UI
- Maintainability: each layer has clear responsibility

---

### UI

**Q: Compose or XML?**
100% Compose. No XML layouts.

**Q: What Compose features do you use?**
Material 3, NavigationSuiteScaffold (adaptive nav), Shared Element Transitions, LazyColumn/LazyRow, HorizontalPager, Scaffold, BottomSheet, collectAsStateWithLifecycle(), custom animations (slide+fade, fade+scale), Lottie animations, edge-to-edge, Splash Screen API.

**Q: How do you handle different screen sizes?**
NavigationSuiteScaffold automatically switches between bottom navigation bar (phones) and navigation rail (tablets) based on WindowWidthSizeClass.

---

### State

**Q: LiveData or StateFlow?**
StateFlow + SharedFlow exclusively. No LiveData.

**Q: Why StateFlow over LiveData?**
- Kotlin-native (works without Android)
- Always has initial value (no null issues)
- Better Compose integration with collectAsStateWithLifecycle()
- Works seamlessly with coroutines

**Q: How do you handle one-shot events (navigation, toasts)?**
SharedFlow for per-screen events, Channel<ToastEvent> for the global toast system (Channel guarantees delivery unlike SharedFlow).

---

### Auth

**Q: How does authentication work?**
Dual-layer: Firebase Auth for identity (Google + Email/Password) → exchange Firebase ID token for custom backend JWT (access + refresh tokens) → stored in EncryptedSharedPreferences.

**Q: How do you handle token expiry?**
OkHttp TokenAuthenticator intercepts 401 → calls refresh endpoint → retries original request. If refresh fails → auto sign-out.

**Q: Why not just use Firebase Auth tokens directly?**
Our backend is custom FastAPI, not Firebase. We need our own JWTs for access control, rate limiting, and server-side user management.

**Q: How do you store tokens securely?**
EncryptedSharedPreferences with AES256 encryption (key scheme: AES256-SIV, value scheme: AES256-GCM). Regular SharedPreferences would store them in plain text.

---

### Networking

**Q: What do you use for networking?**
Retrofit 2.11.0 + OkHttp 4.12.0 + kotlinx.serialization (not Gson or Moshi).

**Q: Why kotlinx.serialization over Gson?**
Kotlin-first, more type-safe, better performance, works with @Serializable annotation on data classes, same serialization for network + navigation routes.

**Q: How does the chat streaming work?**
SSE (Server-Sent Events) via raw OkHttp + okhttp-sse library, wrapped in Kotlin callbackFlow. Server sends tokens one-by-one → UI updates in real-time (like ChatGPT).

**Q: What interceptors do you use?**
AuthInterceptor (adds Bearer token), RateLimitInterceptor (handles 429), HttpLoggingInterceptor (debug only), TokenAuthenticator (auto-refresh on 401).

---

### Database

**Q: What database do you use?**
Room 2.7.1 (SQLite wrapper). 6 tables including FTS4 virtual table for full-text search on hieroglyphic signs.

**Q: What is FTS4?**
Full-Text Search 4 — a SQLite extension that creates a special virtual table optimized for text search. Users can search hieroglyphic signs by code, meaning, description, etc. Much faster than LIKE queries.

**Q: How do you handle migrations?**
Room migrations with explicit SQL migration paths (version 4→5, etc.). Schema exports enabled for validation.

---

### DI

**Q: What DI framework?**
Hilt 2.53.1 with KSP (not KAPT — 2x faster).

**Q: Why Hilt over Koin?**
Compile-time verification (catches errors at build time, not runtime), better performance (no reflection), official Google recommendation for Android.

---

### Testing

**Q: What testing frameworks do you use?**
JUnit 4, MockK (Kotlin mocking), Turbine (Flow testing), kotlinx-coroutines-test, MockWebServer (network), Room in-memory DB (DAO tests), Espresso + Compose UI Test (instrumented).

**Q: What do you test?**
Repository implementations (unit), OkHttp interceptors (unit), pronunciation utility (unit), Room DAOs (instrumented with in-memory DB).

---

### Firebase

**Q: What Firebase services do you use?**
Auth (identity), Firestore (story progress sync), FCM (push notifications), Analytics, Crashlytics (release crash reports).

**Q: Why Firestore for story progress?**
Real-time sync across devices — when user reads a story on one device, progress updates on all devices.

---

### Performance & Best Practices

**Q: How do you handle image loading?**
Coil 3 (coil-compose) — Kotlin-first image loader. Custom SingletonImageLoader in WadjetApplication with shared OkHttpClient. BaseUrlInterceptor prepends backend URL to relative image paths.

**Q: How do you handle offline support?**
Landmarks and dictionary signs cached in Room database. NetworkMonitor (ConnectivityManager) detects network state. OfflineIndicator composable shown when offline.

**Q: How do you handle configuration changes?**
ViewModels survive configuration changes. StateFlow retains state. Compose automatically recomposes with the same state.

**Q: What's your minSdk and why?**
26 (Android 8.0) — allows EncryptedSharedPreferences, avoids pre-Oreo compatibility headaches, covers 95%+ of active devices.

---

### Project-Specific

**Q: What makes this app unique?**
- AI-powered hieroglyph recognition from photos (ONNX + Gemini + Grok ensemble)
- Chat with AI Egyptologist using SSE streaming
- Egyptian pronunciation engine (MdC → speakable English)
- AI-generated story scene images
- Dark Egyptian aesthetic with custom gold-on-black theme
- Dual auth (Firebase + custom JWT)

**Q: What was the most challenging part?**
- Chat SSE streaming with token-by-token UI updates using callbackFlow
- Dual auth system with automatic token refresh and race condition handling
- Egyptian pronunciation conversion (transliteration → phonetically correct English)
- Coordinating 18 modules with proper dependency boundaries

**Q: What would you improve?**
- Enable on-device ML (ONNX) for offline hieroglyph recognition
- Add CameraX live scanning (code exists but disabled)
- Add light mode theme option
- Increase test coverage for UI/integration tests
- Add Baseline Profiles for startup performance

---

## 19. Project Progress — "وصلت لفين؟"

### EN — What I've Built So Far

The project was built in structured phases over ~3 weeks. Here's what's done:

#### Phase 0: Project Setup ✅
- Created 18-module Gradle project (1 app + 7 core + 10 feature)
- Set up Firebase, downloaded fonts, ONNX models, .gitignore rules, launcher icons

#### Phase 1: Design System ✅
- Built custom dark Egyptian theme (WadjetTheme)
- Gold/black color palette, 5 font families (Playfair, Inter, JetBrains Mono, Noto Egyptian Hieroglyphs, Cairo)
- All reusable components: WadjetButton, WadjetCard, WadjetTopBar, WadjetTextField, etc.

#### Phase 2: Authentication ✅
- Firebase Auth (Google Sign-In via Credential Manager + Email/Password)
- Backend JWT exchange (access + refresh tokens)
- EncryptedSharedPreferences, AuthInterceptor, TokenAuthenticator (auto-refresh)
- Welcome screen with Login/Register/ForgotPassword bottom sheets

#### Phase 3: Dictionary ✅
- 3 tabs: Browse (Room FTS4 + API search), Learn (alphabet + lessons), Write (English → hieroglyphs via AI)
- Sign detail sheet with Gardiner code, glyph, transliteration, meaning
- Room database with FTS4 full-text search virtual table

#### Phase 4: Scanner ✅
- Image upload → `POST /api/scan?mode=auto` → annotated result
- ScanResultScreen: annotated image, glyph chips row, transliteration + TTS, translations (EN/AR), confidence, AI notes
- Scan history saved to Room with thumbnails

#### Phase 5: Explore & Landmarks ✅
- Explore screen with paginated grid, category/city filters, search with debounce
- LandmarkDetailScreen: image carousel (HorizontalPager), sections, highlights, tips, Google Maps, "Ask Thoth", recommendations
- Identify screen: upload photo → AI ensemble → top-3 landmark matches
- Landmarks cached in Room for offline browsing
- Favorites system

#### Phase 6: Chat (Thoth AI) ✅
- SSE streaming via raw OkHttp EventSource + callbackFlow
- Token-by-token UI updates (like ChatGPT)
- Markdown rendering, TTS per message, STT input
- Message edit (ChatGPT-style: long-press Edit → truncate history → resend)
- Chat history stored as JSON files, conversation list panel
- Landmark-specific chat mode

#### Phase 7: Stories ✅
- Stories list filtered by difficulty (Beginner/Intermediate/Advanced)
- StoryReaderScreen: chapter-by-chapter, scene images (AI-generated on demand), TTS narration
- 4 interaction types per chapter
- Progress tracked in Room + Firestore sync

#### Phase 8: Landing + Dashboard + Settings + Feedback ✅
- Landing: greeting, usage card, recent scan, in-progress story, quick-action grid
- Dashboard: stats, scan history, favorites (landmark/glyph/story tabs), story progress
- Settings: display name, TTS config, password change, sign out, cache management
- Feedback: bug report / feedback form → POST /api/feedback

#### Spec 002: UX Fixes ✅ (Committed)
- Toast system (ToastController singleton → WadjetToast overlay)
- Story cover glyph font fix (NotoSansEgyptianHieroglyphs)
- Dictionary sound button guard
- Translate tab removed (4→3 tabs)
- Write TTS sends transliteration not raw Unicode
- TTS loading/error/success toasts across all screens
- Scan RTL layout for Arabic
- Landmark image fallback chain (images → thumbnail → originalImage → placeholder)
- Story scene image loading with SharedPreferences cache + retry
- Chat: long-press copy, relative timestamps, retry on fail, typing indicator, scroll-to-bottom FAB
- Chat message edit (ChatGPT-style)
- Write pronunciation mapping (MdC → speakable English)
- AuthInterceptor scoped to API base URL only (was leaking tokens to Wikipedia)

#### Spec 003: UX Redesign ✅ (5 phases, committed)
- Phase 1: Design system fixes — typography, color tokens, shape migration, new components (WadjetAsyncImage, WadjetSearchBar, shimmer placeholders)
- Phase 2: String extraction into per-module `strings.xml` + Arabic stub files (localization infrastructure)
- Phase 3: Navigation + platform polish — splash screen, back-gesture, navigation back-stack preservation, auth fixes, chat IME insets, accessibility
- Phase 4: Interaction & content UX — pull-to-refresh, HieroglyphsHub enrichment, scan history improvements
- Phase 5: Visual polish — shared element transitions, adaptive layout (NavigationSuiteScaffold), animations

#### Spec 004: Logic Quality ✅ (8 task groups, committed)
- T001-T011: Pronunciation engine hardened — fixed tokenizer, Gardiner Unicode mapping, comprehensive unit tests
- T020-T035: DTO→Domain mapping alignment — added missing fields to Sign, InteractionResult, ScanApiService fixes, Room migration v4→v7
- T040-T058: ViewModel lifecycle safety — fixed MediaPlayer leaks, removed GlobalScope usage, thread correctness
- T060-T075: Auth fixes — resolved split-brain token state, replaced blocking interceptors
- T080-T092: Offline improvements — upgraded FTS, added offline fallbacks, improved caching
- T100-T118: Chat session fixes, dead code cleanup, error message consistency
- T120-T128: Comprehensive repository test coverage (unit tests with MockK + Turbine)
- Final: Room downgraded 2.8.4→2.7.1 for Kotlin 2.1.0 compatibility, fixed MainActivity crash

### Summary Table

| Phase / Spec | Status | Commits |
|---|---|---|
| P0: Project Setup | ✅ Done | 3 commits |
| P1: Design System | ✅ Done | 1 commit |
| P2: Auth & Navigation | ✅ Done | 2 commits |
| P3: Dictionary | ✅ Done | 1 commit |
| P4: Scanner | ✅ Done | 1 commit |
| P5: Explore & Landmarks | ✅ Done | 1 commit |
| P6: Chat (Thoth AI) | ✅ Done | 1 commit |
| Quality Pass | ✅ Done | 2 commits |
| Spec 002: UX Fixes | ✅ Done | 3 commits |
| Spec 003: UX Redesign (5 phases) | ✅ Done | 8 commits |
| Spec 004: Logic Quality (8 groups) | ✅ Done | 8 commits |
| Spec 005: Full Testing | 📋 Planned | Not started |
| Spec 006: Bug Fixes | 📋 Planned | Not started |
| **Total commits** | | **~31 commits** |

---

### AR — وصلت لفين؟

المشروع اتبنى في phases منظمة على مدار ~3 أسابيع:

#### Phase 0: إعداد المشروع ✅
- عملت 18 module (1 app + 7 core + 10 feature)
- ضبطت Firebase، نزّلت الخطوط، ONNX models، أيقونات التطبيق

#### Phase 1: الـ Design System ✅
- بنيت ثيم مصري dark كامل (WadjetTheme)
- ألوان دهبي/أسود، 5 عائلات خطوط
- كل الكومبوننتس المشتركة: WadjetButton, WadjetCard, WadjetTopBar, إلخ.

#### Phase 2: الأوثنتكيشن ✅
- Firebase Auth (Google Sign-In + Email/Password)
- تبادل JWT مع الـ backend (access + refresh tokens)
- تخزين مشفر بـ EncryptedSharedPreferences
- TokenAuthenticator بيعمل refresh تلقائي
- شاشة الترحيب مع 3 bottom sheets (Login, Register, ForgotPassword)

#### Phase 3: القاموس ✅
- 3 tabs: تصفح (Room FTS4 + API)، تعلم (أبجدية + دروس)، كتابة (إنجليزي → هيروغليفي بـ AI)
- شاشة تفاصيل الرمز
- Room database مع FTS4 للبحث النصي السريع

#### Phase 4: الماسح الضوئي ✅
- رفع صورة → الـ backend بيشغّل AI → نتيجة annotated
- شاشة النتيجة: صورة annotated، شريط رموز، ترجمة + TTS، ترجمات إنجليزي وعربي
- تاريخ الـ scans متحفظ في Room

#### Phase 5: الاستكشاف والمعالم ✅
- شبكة معالم مع فلتر أنواع/مدن + بحث
- شاشة التفاصيل: كاروسيل صور، أقسام، Google Maps، "اسأل تحوت"
- التعرف على المعالم: ارفع صورة → AI → أفضل 3 نتايج
- المعالم متخزنة في Room عشان offline
- نظام المفضلة

#### Phase 6: الشات مع تحوت AI ✅
- SSE streaming — الكلام بييجي token بـ token (زي ChatGPT)
- Markdown rendering، TTS لكل رسالة، STT
- تعديل الرسالة (زي ChatGPT)
- تاريخ المحادثات
- وضع المعالم (شات مخصص عن معلم معين)

#### Phase 7: القصص ✅
- قائمة مفلترة حسب الصعوبة
- قارئ القصص: فصل بفصل، صور المشاهد (AI)، سرد صوتي
- تقدم متحفظ في Room + Firestore

#### Phase 8: الصفحة الرئيسية + لوحة التحكم + الإعدادات + التغذية الراجعة ✅

#### Spec 002: إصلاحات UX ✅
- نظام Toast، إصلاح خطوط الهيروغليفي، TTS في كل مكان
- تعديل رسائل الشات، إصلاح AuthInterceptor
- RTL للعربي في الـ Scan

#### Spec 003: إعادة تصميم UX ✅ (5 مراحل)
- إصلاح الـ design system، string extraction للترجمة، Navigation polish
- Shared element transitions، adaptive layout للتابلت
- Pull-to-refresh، accessibility

#### Spec 004: جودة المنطق ✅ (8 مجموعات مهام)
- إصلاح محرك النطق المصري + اختبارات شاملة
- محاذاة DTO→Domain (أضفت fields ناقصة)
- أمان lifecycle الـ ViewModels (إصلاح تسريب MediaPlayer، إزالة GlobalScope)
- إصلاح Auth split-brain
- تحسين Offline + FTS
- تنظيف كود ميت + توحيد رسائل الخطأ
- اختبارات repositories شاملة

#### لسه فاضل:
- **Spec 005**: Full Testing — ~337 اختبار مخطط (200 unit + 50 screenshot + 20 UI + ...)
- **Spec 006**: Bug fixes إضافية — 45 fix مخطط

---

### لو حد سألك:

**"وصلت لفين؟"**
> "الأبليكيشن شغال بالكامل — كل الـ features الأساسية متعملة: Auth, Scan, Dictionary, Chat, Stories, Explore, Dashboard, Settings. عديت على 4 specs بعد كده (UX Fixes, UX Redesign, Logic Quality) — كل واحدة فيها phases متعددة. حالياً في مرحلة الـ Testing والـ final bug fixes."

**"كام commit؟"**
> "~31 commit منظمين في phases + specs."

**"إيه اللي شغال في الأبليكيشن دلوقتي؟"**
> "كل حاجة شغالة: الأوثنتكيشن بـ Google و Email، الـ Scan بيتعرف على الهيروغليفي من الصور، القاموس فيه بحث FTS4 ودروس وكتابة هيروغليفي بـ AI، الشات مع تحوت AI بـ SSE streaming زي ChatGPT، القصص التفاعلية مع صور AI وسرد صوتي، استكشاف المعالم مع offline cache، والـ Dashboard و Settings و Feedback."

**"إيه اللي فاضل؟"**
> "الـ Testing الشامل (~337 اختبار) وبعض الـ bug fixes الإضافية. والـ Camera (CameraX) موجود بس مش مفعّل — حالياً بنعتمد على gallery upload."
