# Wadjet Android — API Mapping

> Complete mapping of every Wadjet backend API endpoint to Android Retrofit interfaces.
> Base URL: `https://nadercr7-wadjet-v2.hf.space` (configurable via `BuildConfig.BASE_URL`)

---

## Authentication Headers

```kotlin
// All authenticated requests need:
Authorization: Bearer <access_token>

// Refresh token is managed separately (stored in EncryptedSharedPreferences)
// CSRF not needed for mobile — the web CSRF middleware should skip non-browser clients
// Set a custom User-Agent to identify mobile:
User-Agent: Wadjet-Android/1.0.0
```

---

## Retrofit Service Interfaces

### AuthApiService

```kotlin
interface AuthApiService {

    @POST("/api/auth/register")
    suspend fun register(
        @Body body: RegisterRequest  // { email, password, display_name? }
    ): AuthResponse  // { access_token, token_type, user: UserResponse }

    @POST("/api/auth/login")
    suspend fun login(
        @Body body: LoginRequest  // { email, password }
    ): AuthResponse

    @POST("/api/auth/google")
    suspend fun googleSignIn(
        @Body body: GoogleAuthRequest  // { credential: "google_id_token" }
    ): AuthResponse

    @POST("/api/auth/refresh")
    suspend fun refreshToken(
        @Header("Cookie") refreshCookie: String  // "wadjet_refresh=<token>"
    ): RefreshResponse  // { access_token, token_type }

    @POST("/api/auth/logout")
    suspend fun logout(
        @Header("Cookie") refreshCookie: String
    ): DetailResponse  // { detail: "Logged out" }

    @POST("/api/auth/send-verification")
    suspend fun sendVerification(): DetailResponse

    @POST("/api/auth/verify-email")
    suspend fun verifyEmail(
        @Body body: VerifyEmailRequest  // { token }
    ): DetailResponse

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequest  // { email }
    ): DetailResponse

    @POST("/api/auth/reset-password")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequest  // { token, new_password }
    ): DetailResponse
}
```

**Data Models:**
```kotlin
@Serializable
data class RegisterRequest(val email: String, val password: String, val display_name: String? = null)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class GoogleAuthRequest(val credential: String)

@Serializable
data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val display_name: String?,
    val preferred_lang: String,
    val tier: String,
    val auth_provider: String,  // "email" | "google" | "both"
    val email_verified: Boolean,
    val avatar_url: String?,
    val created_at: String
)
```

---

### UserApiService

```kotlin
interface UserApiService {

    @GET("/api/user/profile")
    suspend fun getProfile(): UserResponse

    @PATCH("/api/user/profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest  // { display_name?, preferred_lang?: "en"|"ar" }
    ): UserResponse

    @PATCH("/api/user/password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest  // { current_password, new_password }
    ): OkResponse  // { ok: true }

    @GET("/api/user/history")
    suspend fun getScanHistory(): List<ScanHistoryItem>

    @GET("/api/user/favorites")
    suspend fun getFavorites(): List<FavoriteItem>

    @POST("/api/user/favorites")
    suspend fun addFavorite(
        @Body body: AddFavoriteRequest  // { item_type, item_id }
    ): FavoriteItem

    @DELETE("/api/user/favorites/{item_type}/{item_id}")
    suspend fun removeFavorite(
        @Path("item_type") itemType: String,
        @Path("item_id") itemId: String
    ): OkResponse

    @GET("/api/user/stats")
    suspend fun getStats(): UserStats

    @GET("/api/user/progress")
    suspend fun getStoryProgress(): List<StoryProgressItem>

    @POST("/api/user/progress")
    suspend fun updateStoryProgress(
        @Body body: UpdateProgressRequest
    ): StoryProgressItem

    @GET("/api/user/limits")
    suspend fun getLimits(): UserLimits
}
```

**Data Models:**
```kotlin
@Serializable
data class ScanHistoryItem(
    val id: Int,
    val results_json: String,
    val confidence_avg: Float,
    val glyph_count: Int,
    val created_at: String
)

@Serializable
data class FavoriteItem(
    val id: Int,
    val item_type: String,  // "landmark" | "glyph" | "story"
    val item_id: String,
    val created_at: String
)

@Serializable
data class UserStats(
    val scans_today: Int,
    val total_scans: Int,
    val total_stories_completed: Int
)

@Serializable
data class UserLimits(
    val tier: String,
    val limits: LimitsDetail,
    val usage: UsageDetail
)
```

---

### ScanApiService

```kotlin
interface ScanApiService {

    @Multipart
    @POST("/api/scan")
    suspend fun scan(
        @Part file: MultipartBody.Part,             // image file
        @Part("mode") mode: RequestBody = "auto"     // "ai" | "onnx" | "auto"
    ): ScanResponse

    @Multipart
    @POST("/api/detect")
    suspend fun detect(
        @Part file: MultipartBody.Part
    ): DetectResponse

    @Multipart
    @POST("/api/read")
    suspend fun aiRead(
        @Part file: MultipartBody.Part
    ): ReadResponse
}
```

**Data Models:**
```kotlin
@Serializable
data class ScanResponse(
    val num_detections: Int,
    val glyphs: List<DetectedGlyph>,
    val transliteration: String?,
    val gardiner_sequence: String?,
    val reading_direction: String?,
    val layout_mode: String?,
    val translation_en: String?,
    val translation_ar: String?,
    val detection_ms: Long,
    val classification_ms: Long,
    val transliteration_ms: Long?,
    val translation_ms: Long?,
    val total_ms: Long,
    val annotated_image: String?,  // base64 JPEG
    val mode: String,
    val pipeline: String?
)

@Serializable
data class DetectedGlyph(
    val bbox: List<Float>,  // [x1, y1, x2, y2]
    val detection_confidence: Float,
    val gardiner_code: String,
    val class_confidence: Float
)
```

---

### TranslateApiService

```kotlin
interface TranslateApiService {

    @POST("/api/translate")
    suspend fun translate(
        @Body body: TranslateRequest  // { transliteration, gardiner_sequence? }
    ): TranslateResponse
}

@Serializable
data class TranslateRequest(
    val transliteration: String,  // max 2000 chars
    val gardiner_sequence: String? = null
)

@Serializable
data class TranslateResponse(
    val transliteration: String,
    val english: String?,
    val arabic: String?,
    val context: String?,
    val error: String?,
    val provider: String?,
    val latency_ms: Long?,
    val from_cache: Boolean?
)
```

---

### DictionaryApiService

```kotlin
interface DictionaryApiService {

    @GET("/api/dictionary")
    suspend fun getSigns(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,  // uniliteral|biliteral|triliteral|logogram|determinative
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,  // max 200
        @Query("lang") lang: String = "en"
    ): DictionaryResponse

    @GET("/api/dictionary/categories")
    suspend fun getCategories(
        @Query("lang") lang: String = "en"
    ): CategoriesResponse

    @GET("/api/dictionary/alphabet")
    suspend fun getAlphabet(
        @Query("lang") lang: String = "en"
    ): AlphabetResponse

    @GET("/api/dictionary/lesson/{level}")
    suspend fun getLesson(
        @Path("level") level: Int,  // 1..5
        @Query("lang") lang: String = "en"
    ): LessonResponse

    @GET("/api/dictionary/{code}")
    suspend fun getSign(
        @Path("code") code: String,  // e.g. "G1"
        @Query("lang") lang: String = "en"
    ): SignDetail
}
```

**Data Models:**
```kotlin
@Serializable
data class DictionaryResponse(
    val signs: List<SignDetail>,
    val total: Int,
    val page: Int,
    val per_page: Int,
    val total_pages: Int
)

@Serializable
data class SignDetail(
    val code: String,           // "G1"
    val glyph: String,          // "𓄿" (Unicode)
    val transliteration: String,
    val phonetic_value: String?,
    val meaning: String,
    val type: String,           // uniliteral|biliteral|triliteral|logogram|determinative
    val category: String,       // "G"
    val category_name: String,  // "Birds"
    val examples: List<String>?,
    val fun_fact: String?,
    val speech: String?,
    val pronunciation_guide: PronunciationGuide?
)

@Serializable
data class PronunciationGuide(
    val sound: String,
    val description: String
)
```

---

### WriteApiService

```kotlin
interface WriteApiService {

    @POST("/api/write")
    suspend fun write(
        @Body body: WriteRequest  // { text (1-500), mode }
    ): WriteResponse

    @GET("/api/write/palette")
    suspend fun getPalette(): PaletteResponse
}

@Serializable
data class WriteRequest(
    val text: String,  // 1-500 chars
    val mode: String   // "alpha" | "mdc" | "smart"
)

@Serializable
data class WriteResponse(
    val hieroglyphs: String,  // "𓄿𓂋..."
    val glyphs: List<WriteGlyph>,
    val mode: String,
    val mdc: String?
)

@Serializable
data class WriteGlyph(
    val gardiner_code: String,
    val glyph: String,
    val transliteration: String?,
    val phonetic_value: String?,
    val meaning: String?
)
```

---

### LandmarkApiService

```kotlin
interface LandmarkApiService {

    @GET("/api/landmarks")
    suspend fun getLandmarks(
        @Query("category") category: String? = null,
        @Query("city") city: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 24,  // max 100
        @Query("lang") lang: String = "en",
        @Query("featured") featured: Boolean? = null
    ): LandmarkListResponse

    @GET("/api/landmarks/categories")
    suspend fun getCategories(): LandmarkCategoriesResponse

    @GET("/api/landmarks/{slug}")
    suspend fun getLandmarkDetail(
        @Path("slug") slug: String,
        @Query("lang") lang: String = "en"
    ): LandmarkDetail

    @GET("/api/landmarks/{slug}/children")
    suspend fun getLandmarkChildren(
        @Path("slug") slug: String
    ): LandmarkChildrenResponse

    @Multipart
    @POST("/api/explore/identify")
    suspend fun identifyLandmark(
        @Part file: MultipartBody.Part
    ): IdentifyResponse
}
```

**Data Models:**
```kotlin
@Serializable
data class LandmarkListResponse(
    val landmarks: List<LandmarkSummary>,
    val total: Int,
    val page: Int,
    val per_page: Int,
    val total_pages: Int
)

@Serializable
data class LandmarkSummary(
    val slug: String,
    val name: String,
    val name_ar: String?,
    val city: String?,
    val type: String?,
    val era: String?,
    val thumbnail: String?,
    val featured: Boolean?,
    val popularity: Int?
)

@Serializable
data class LandmarkDetail(
    val slug: String,
    val name: String,
    val name_ar: String?,
    val city: String?,
    val type: String?,
    val subcategory: String?,
    val era: String?,
    val period: String?,
    val popularity: Int?,
    val description: String?,
    val coordinates: List<Double>?,  // [lat, lng]
    val maps_url: String?,
    val thumbnail: String?,
    val tags: List<String>?,
    val related_sites: List<String>?,
    val featured: Boolean?,
    val images: List<LandmarkImage>?,
    val sections: List<LandmarkSection>?,
    val highlights: String?,
    val visiting_tips: String?,
    val historical_significance: String?,
    val wikipedia_url: String?,
    val recommendations: List<Recommendation>?
)

@Serializable
data class LandmarkImage(val url: String, val caption: String?)

@Serializable
data class LandmarkSection(val title: String, val content: String)

@Serializable
data class Recommendation(
    val slug: String,
    val name: String,
    val score: Float,
    val reasons: List<String>
)

@Serializable
data class IdentifyResponse(
    val slug: String?,
    val name: String?,
    val confidence: Float,
    val top3: List<IdentifyMatch>,
    val landmark: LandmarkDetail?
)

@Serializable
data class IdentifyMatch(
    val slug: String,
    val name: String,
    val confidence: Float
)
```

---

### ChatApiService

```kotlin
interface ChatApiService {

    @POST("/api/chat")
    suspend fun chat(
        @Body body: ChatRequest
    ): ChatResponse

    // SSE streaming — use OkHttp directly, not Retrofit
    // POST /api/chat/stream { message, session_id, landmark? }
    // Response: text/event-stream

    @POST("/api/chat/clear")
    suspend fun clearChat(
        @Body body: ClearChatRequest  // { session_id }
    ): DetailResponse
}

@Serializable
data class ChatRequest(
    val message: String,       // 1-2000 chars
    val session_id: String,    // 1-128 chars
    val landmark: String? = null
)

@Serializable
data class ChatResponse(
    val reply: String,
    val sources: List<String>?
)
```

**SSE Streaming (OkHttp):**
```kotlin
// This is NOT a Retrofit call — use OkHttp directly
suspend fun chatStream(
    message: String,
    sessionId: String,
    landmark: String? = null,
    onChunk: (String) -> Unit,
    onDone: () -> Unit,
    onError: (Exception) -> Unit
) {
    val body = Json.encodeToString(ChatRequest(message, sessionId, landmark))
    val request = Request.Builder()
        .url("${BASE_URL}/api/chat/stream")
        .post(body.toRequestBody("application/json".toMediaType()))
        .addHeader("Authorization", "Bearer $token")
        .build()
    
    // Parse SSE: "data: {\"text\": \"chunk\"}\n\n"
    // Terminal: "data: [DONE]\n\n"
}
```

---

### StoriesApiService

```kotlin
interface StoriesApiService {

    @GET("/api/stories")
    suspend fun getStories(): StoriesListResponse

    @GET("/api/stories/{storyId}")
    suspend fun getStory(
        @Path("storyId") storyId: String
    ): StoryFull

    @GET("/api/stories/{storyId}/chapters/{index}")
    suspend fun getChapter(
        @Path("storyId") storyId: String,
        @Path("index") index: Int
    ): ChapterResponse

    @POST("/api/stories/{storyId}/interact")
    suspend fun interact(
        @Path("storyId") storyId: String,
        @Body body: InteractRequest
    ): InteractResponse

    @POST("/api/stories/{storyId}/chapters/{index}/image")
    suspend fun generateChapterImage(
        @Path("storyId") storyId: String,
        @Path("index") index: Int
    ): ChapterImageResponse
}
```

**Data Models:**
```kotlin
@Serializable
data class StoriesListResponse(
    val stories: List<StorySummary>,
    val count: Int
)

@Serializable
data class StorySummary(
    val id: String,
    val title: BilingualText,      // { en, ar }
    val subtitle: BilingualText,
    val cover_glyph: String,
    val difficulty: String,
    val estimated_minutes: Int,
    val chapter_count: Int,
    val glyphs_taught: List<String>
)

@Serializable
data class BilingualText(val en: String, val ar: String)

@Serializable
data class InteractRequest(
    val chapter_index: Int,
    val interaction_index: Int,
    val answer: String
)

@Serializable
data class InteractResponse(
    val correct: Boolean,
    val type: String,
    val explanation: String?,
    val outcome: String?
)

@Serializable
data class ChapterImageResponse(
    val image_url: String,
    val status: String
)
```

---

### AudioApiService

```kotlin
interface AudioApiService {

    @Multipart
    @POST("/api/stt")
    suspend fun speechToText(
        @Part file: MultipartBody.Part,
        @Part("lang") lang: RequestBody  // "en" | "ar"
    ): SttResponse

    @POST("/api/audio/speak")
    suspend fun speak(
        @Body body: SpeakRequest
    ): ResponseBody  // Raw WAV bytes (200) or empty (204)
}

@Serializable
data class SpeakRequest(
    val text: String,     // 1-5000 chars
    val lang: String,     // "en" | "ar"
    val context: String   // "default"|"thoth_chat"|"story_narration"|"dictionary"|"pronunciation"|"landing"|"explore"|"scan"
)

@Serializable
data class SttResponse(
    val text: String,
    val language: String
)
```

---

### FeedbackApiService

```kotlin
interface FeedbackApiService {

    @POST("/api/feedback")
    suspend fun submit(
        @Body body: FeedbackRequest
    ): FeedbackResponse
}

@Serializable
data class FeedbackRequest(
    val category: String,   // "bug"|"suggestion"|"praise"|"other"
    val message: String,    // 10-1000 chars
    val page_url: String? = null,
    val name: String? = null,
    val email: String? = null
)

@Serializable
data class FeedbackResponse(
    val ok: Boolean,
    val id: Int
)
```

---

### HealthApiService

```kotlin
interface HealthApiService {

    @GET("/health")
    suspend fun check(): HealthResponse
}

@Serializable
data class HealthResponse(
    val version: String,
    val db: DbHealth,
    val ai_providers: Map<String, Boolean>,
    val timestamp: String
)
```

---

## Rate Limit Reference

| Endpoint | Limit | Response on Exceed |
|----------|-------|--------------------|
| `/api/auth/register` | 5/min | 429 |
| `/api/auth/login` | 10/min (+ lockout after 10 failures) | 429 |
| `/api/auth/refresh` | 10/min | 429 |
| `/api/auth/send-verification` | 3/min | 429 |
| `/api/auth/forgot-password` | 3/min | 429 |
| `/api/translate` | 30/min | 429 |
| `/api/landmarks/*` | 60/min | 429 |
| `/api/chat`, `/api/chat/stream` | 30/min | 429 |
| `/api/stories/*` | 60/min | 429 |
| `/api/stories/*/interact` | 120/min | 429 |
| `/api/stories/*/image` | 10/min | 429 |
| `/api/stt` | 10/min | 429 |
| `/api/audio/speak` | 20/min | 429 |
| `/api/feedback` | 5/min | 429 |

---

## Image Upload Guidelines

- Max size: **10 MB**
- Min dimensions: **32×32 px**
- Max dimensions: auto-resized to **1024px** longest side
- Formats: JPEG, PNG, WebP, HEIC
- Compress on client to ~85% JPEG quality before uploading
- Use `BitmapFactory.Options.inSampleSize` for large camera images
