package app.neono.api

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Generate ID unik (NEO-XXXX)
    fun generateUserCode(): String {
        val number = Random.nextInt(1000, 9999)
        return "NEO-$number"
    }

    // Simpan userCode ke Firestore (dipanggil saat daftar)
    suspend fun saveUserCode(uid: String, userCode: String) {
        db.collection("users").document(uid).set(
            mapOf("userCode" to userCode, "uid" to uid)
        ).await()
    }

    // Cari user berdasarkan userCode (buat tambah kontak)
    suspend fun findUserByCode(code: String): Map<String, Any>? {
        val snap = db.collection("users").whereEqualTo("userCode", code).get().await()
        if (snap.isEmpty) return null
        val doc = snap.documents.first()
        return mapOf(
            "uid" to doc.id,
            "userCode" to (doc.getString("userCode") ?: ""),
            "displayName" to (doc.getString("displayName") ?: "User")
        )
    }

    // Bikin room chat 1-on-1 (kalau belum ada)
    suspend fun createDirectChat(myUid: String, otherUid: String): String {
        val chatId = listOf(myUid, otherUid).sorted().joinToString("_")
        val ref = db.collection("chats").document(chatId)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(mapOf(
                "type" to "direct",
                "members" to listOf(myUid, otherUid),
                "updatedAt" to System.currentTimeMillis()
            )).await()
        }
        return chatId
    }
}
