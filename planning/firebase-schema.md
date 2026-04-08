# Wadjet Android — Firebase Schema

> Firestore collections, security rules, and Firebase Auth configuration.

---

## Firebase Auth Configuration

### Enabled Providers
1. **Google Sign-In** (primary) — SHA-1 fingerprint required in Firebase Console
2. **Email/Password** (secondary) — with email verification
3. **Email Link** — for password reset flows

### Firebase Auth → Wadjet Backend Sync

On every Firebase auth event, the app ALSO authenticates with the Wadjet backend:

```
Firebase Auth login/register
       ↓
Get Google ID token or Firebase ID token
       ↓
POST /api/auth/google { credential: google_id_token }
  or
POST /api/auth/register / /api/auth/login
       ↓
Receive Wadjet access_token + refresh_token
       ↓
Store both in EncryptedSharedPreferences
```

This dual-auth ensures:
- Firebase handles user management, offline auth state
- Wadjet backend validates requests and tracks usage/limits
- Both systems have the user account

---

## Firestore Database Structure

### Collection: `users/{userId}`

**Document ID**: Firebase Auth UID

```json
{
  "email": "nader@example.com",
  "display_name": "Nader",
  "preferred_lang": "en",
  "tier": "free",
  "auth_provider": "google",
  "avatar_url": "https://...",
  "wadjet_backend_id": "abc123hex",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

### Subcollection: `users/{userId}/scan_history/{scanId}`

```json
{
  "thumbnail_url": "local_uri_or_cloud_url",
  "glyph_count": 5,
  "confidence_avg": 0.89,
  "gardiner_sequence": "G1-D21-M17",
  "transliteration": "A r i",
  "translation_en": "The great...",
  "translation_ar": "العظيم...",
  "pipeline": "auto",
  "total_ms": 970,
  "created_at": "Timestamp"
}
```

**Note**: The full `results_json` (base64 annotated image, bounding boxes) is stored locally in Room, NOT in Firestore (too large). Firestore stores only the summary metadata for sync.

### Subcollection: `users/{userId}/favorites/{favoriteId}`

```json
{
  "item_type": "landmark",    // "landmark" | "glyph" | "story"
  "item_id": "great-pyramids-of-giza",
  "display_name": "Great Pyramids of Giza",
  "thumbnail": "https://...",
  "created_at": "Timestamp"
}
```

### Subcollection: `users/{userId}/story_progress/{storyId}`

**Document ID**: story ID (e.g., `osiris-myth`)

```json
{
  "story_id": "osiris-myth",
  "chapter_index": 2,
  "glyphs_learned": ["Q1", "N5", "D21"],
  "score": 12,
  "completed": false,
  "updated_at": "Timestamp"
}
```

### Subcollection: `users/{userId}/lesson_progress/{lessonId}`

**Document ID**: lesson level (e.g., `1`, `2`, ..., `5`)

```json
{
  "lesson_id": 1,
  "lesson_title": "The Alphabet",
  "glyphs_mastered": ["A1", "D21", "G1", "M17"],
  "score": 8,
  "max_score": 10,
  "completed": true,
  "attempts": 3,
  "updated_at": "Timestamp"
}
```

### Subcollection: `users/{userId}/chat_sessions/{sessionId}`

**Optional** — for chat history persistence across devices.

```json
{
  "messages": [
    {
      "role": "user",
      "content": "Tell me about Osiris",
      "timestamp": "Timestamp"
    },
    {
      "role": "assistant", 
      "content": "Ah, Osiris! The great king...",
      "timestamp": "Timestamp"
    }
  ],
  "landmark_context": null,
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

### Collection: `feedback/{feedbackId}`

```json
{
  "user_id": "firebase_uid or null",
  "category": "bug",
  "message": "The scan feature...",
  "page_context": "scan_screen",
  "device_info": "Pixel 8 / Android 15",
  "app_version": "1.0.0",
  "created_at": "Timestamp"
}
```

---

## Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      // Users can only read/write their own document
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Scan history subcollection
      match /scan_history/{scanId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Favorites subcollection
      match /favorites/{favoriteId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        // Validate favorite structure
        allow create: if request.auth != null 
          && request.auth.uid == userId
          && request.resource.data.item_type in ['landmark', 'glyph', 'story']
          && request.resource.data.item_id is string
          && request.resource.data.item_id.size() > 0;
      }
      
      // Story progress subcollection
      match /story_progress/{storyId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Chat sessions subcollection
      match /chat_sessions/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        // Limit message array size
        allow write: if request.resource.data.messages.size() <= 50;
      }
    }
    
    // Feedback collection — anyone authenticated can write, only admin reads
    match /feedback/{feedbackId} {
      allow create: if request.auth != null
        && request.resource.data.message is string
        && request.resource.data.message.size() >= 10
        && request.resource.data.message.size() <= 1000
        && request.resource.data.category in ['bug', 'suggestion', 'praise', 'other'];
      allow read: if false; // Admin reads via Firebase Admin SDK (server-side)
    }
    
    // Default deny
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

---

## Firebase Storage Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // User avatars
    match /avatars/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
        && request.auth.uid == userId
        && request.resource.size < 5 * 1024 * 1024  // 5MB max
        && request.resource.contentType.matches('image/.*');
    }
    
    // Scan images (user's own scans)
    match /scans/{userId}/{fileName} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null 
        && request.auth.uid == userId
        && request.resource.size < 10 * 1024 * 1024  // 10MB max
        && request.resource.contentType.matches('image/.*');
    }
    
    // Default deny
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

---

## Firebase Cloud Messaging (FCM)

### Notification Channels (Android)

```kotlin
enum class NotificationChannel(
    val id: String,
    val displayName: String,
    val description: String,
    val importance: Int
) {
    STORY_REMINDER(
        id = "story_reminder",
        displayName = "Story Reminders",
        description = "Reminders to continue your story progress",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    ),
    DAILY_GLYPH(
        id = "daily_glyph",
        displayName = "Daily Hieroglyph",
        description = "Learn a new hieroglyph every day",
        importance = NotificationManager.IMPORTANCE_LOW
    ),
    NEW_CONTENT(
        id = "new_content",
        displayName = "New Content",
        description = "Notifications about new stories and features",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    )
}
```

### Topic Subscriptions

```kotlin
// On login, subscribe to relevant topics
FirebaseMessaging.getInstance().subscribeToTopic("all_users")
FirebaseMessaging.getInstance().subscribeToTopic("lang_${preferredLang}")  // "lang_en" or "lang_ar"
```

---

## Room Database (Local Cache)

Room is used alongside Firestore for:
1. **Offline access** to dictionary signs, landmark metadata, story content
2. **Full scan results** (large JSON + base64 images that shouldn't go to Firestore)
3. **Search indexing** (FTS on landmark names, sign meanings)

### Room Entities

```kotlin
@Entity(tableName = "signs")
data class SignEntity(
    @PrimaryKey val code: String,      // "G1"
    val glyph: String,                  // "𓄿"
    val transliteration: String,
    val phonetic_value: String?,
    val meaning_en: String,
    val meaning_ar: String?,
    val type: String,
    val category: String,
    val category_name: String,
    val examples_json: String?,         // JSON array
    val fun_fact: String?,
    val speech: String?,
    val pronunciation_sound: String?,
    val pronunciation_desc: String?,
    val cached_at: Long                 // System.currentTimeMillis()
)

@Entity(tableName = "landmarks")
data class LandmarkEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val name_ar: String?,
    val city: String?,
    val type: String?,
    val era: String?,
    val thumbnail: String?,
    val popularity: Int?,
    val featured: Boolean,
    val detail_json: String?,           // Full detail JSON (nullable, fetched on demand)
    val cached_at: Long
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title_en: String,
    val title_ar: String,
    val subtitle_en: String?,
    val subtitle_ar: String?,
    val cover_glyph: String,
    val difficulty: String,
    val estimated_minutes: Int,
    val chapter_count: Int,
    val glyphs_taught_json: String,     // JSON array
    val full_json: String?,             // Full story JSON (nullable, fetched on demand)
    val cached_at: Long
)

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val thumbnail_path: String,         // Local file path
    val results_json: String,           // Full scan result JSON
    val glyph_count: Int,
    val confidence_avg: Float,
    val created_at: Long
)
```

### Cache Strategy

| Data | Source | TTL | Refresh |
|------|--------|-----|---------|
| Dictionary signs | API `/api/dictionary` | 30 days | On app update |
| Landmark list | API `/api/landmarks` | 7 days | On pull-to-refresh |
| Landmark detail | API `/api/landmarks/{slug}` | 7 days | On view (if stale) |
| Story list | API `/api/stories` | 7 days | On view |
| Story content | API `/api/stories/{id}` | 30 days | On read |
| Scan results | Local only | Never expires | User deletes |

---

## Firebase Console Setup Checklist

- [ ] Create Firebase project: `wadjet-android`
- [ ] Enable Authentication: Google + Email/Password
- [ ] Create Firestore database (production mode)
- [ ] Deploy Firestore security rules
- [ ] Create Storage bucket
- [ ] Deploy Storage security rules
- [ ] Enable Analytics
- [ ] Enable Crashlytics
- [ ] Enable Cloud Messaging
- [ ] Add Android app: package `com.wadjet.app`
- [ ] Download `google-services.json` → place in `app/`
- [ ] Configure SHA-1 and SHA-256 fingerprints for Google Sign-In
- [ ] Set up App Check (optional, recommended for production)
