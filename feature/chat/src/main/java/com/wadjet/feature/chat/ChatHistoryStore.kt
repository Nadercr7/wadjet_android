package com.wadjet.feature.chat

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.wadjet.core.domain.model.ChatMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ConversationSummary(
    val id: String,
    val title: String,
    val createdAt: Long,
    val messageCount: Int,
)

@Singleton
class ChatHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir get() = File(context.filesDir, "chat_history").also { it.mkdirs() }
    private val masterKeyAlias by lazy { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) }
    private val prefs by lazy {
        context.getSharedPreferences("chat_session", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val SESSION_TTL_MS = 60 * 60 * 1000L // 1 hour
    }

    /** Returns stored sessionId if it exists and is less than 1 hour old, otherwise null. */
    fun getActiveSessionId(): String? {
        val id = prefs.getString(KEY_SESSION_ID, null) ?: return null
        val ts = prefs.getLong(KEY_SESSION_TIMESTAMP, 0L)
        return if (System.currentTimeMillis() - ts < SESSION_TTL_MS) id else null
    }

    fun storeSessionId(sessionId: String) {
        prefs.edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun clearSessionId() {
        prefs.edit()
            .remove(KEY_SESSION_ID)
            .remove(KEY_SESSION_TIMESTAMP)
            .apply()
    }

    suspend fun listConversations(): List<ConversationSummary> = withContext(Dispatchers.IO) {
        dir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    val text = encryptedFile(file).openFileInput().bufferedReader().use { it.readText() }
                    val obj = JSONObject(text)
                    ConversationSummary(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        createdAt = obj.getLong("createdAt"),
                        messageCount = obj.getJSONArray("messages").length(),
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse chat history file: ${file.name}")
                    null
                }
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    suspend fun saveConversation(id: String, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        if (messages.size < 2) return@withContext
        val userMessages = messages.filter { it.role == ChatMessage.Role.USER }
        if (userMessages.isEmpty()) return@withContext
        val title = userMessages.first().content.take(50)
        val obj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("createdAt", System.currentTimeMillis())
            put("messages", JSONArray().apply {
                messages.filter { !it.isStreaming }.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role.name)
                        put("content", msg.content)
                        put("timestamp", msg.timestamp)
                    })
                }
            })
        }
        val target = File(dir, "$id.json")
        if (target.exists()) target.delete()
        encryptedFile(target).openFileOutput().use { it.write(obj.toString().toByteArray()) }
    }

    suspend fun loadConversation(id: String): List<ChatMessage>? = withContext(Dispatchers.IO) {
        val file = File(dir, "$id.json")
        if (!file.exists()) return@withContext null
        try {
            val text = encryptedFile(file).openFileInput().bufferedReader().use { it.readText() }
            val obj = JSONObject(text)
            val arr = obj.getJSONArray("messages")
            (0 until arr.length()).map { i ->
                val msg = arr.getJSONObject(i)
                ChatMessage(
                    id = "${id}_${i}_${msg.getLong("timestamp")}",
                    role = ChatMessage.Role.valueOf(msg.getString("role")),
                    content = msg.getString("content"),
                    timestamp = msg.getLong("timestamp"),
                )
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to load conversation: $id")
            null
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { it.delete() }
    }

    suspend fun loadLatestConversation(): Pair<String, List<ChatMessage>>? = withContext(Dispatchers.IO) {
        val latest = dir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    val text = encryptedFile(file).openFileInput().bufferedReader().use { it.readText() }
                    val obj = JSONObject(text)
                    obj.getString("id") to obj.getLong("createdAt")
                } catch (_: Exception) {
                    null
                }
            }
            ?.maxByOrNull { it.second }
            ?: return@withContext null
        val messages = loadConversation(latest.first) ?: return@withContext null
        latest.first to messages
    }

    private fun encryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
}
