package app.neono.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Msg(val id: String, val senderId: String, val text: String)

@Composable
fun ChatScreen(myUid: String) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<Msg>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    val chatId = "GLOBAL"

    LaunchedEffect(Unit) {
        // Bikin grup global (kalau belum ada)
        val ref = db.collection("chats").document(chatId)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(mapOf(
                "type" to "group",
                "name" to "Neono Official",
                "members" to listOf(myUid),
                "isOfficial" to true
            )).await()
        } else {
            ref.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(myUid)).await()
        }

        // Listener pesan
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                messages = snap?.documents?.mapNotNull {
                    Msg(it.id, it.getString("senderId") ?: "", it.getString("textEnc") ?: "")
                } ?: emptyList()
                scope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
            }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0B141A))) {
        // HEADER
        Row(Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Neono Official", fontSize = 18.sp, color = Color.White)
        }

        // LIST PESAN
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(messages.size) { i ->
                val m = messages[i]
                val isMe = m.senderId == myUid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
                    Box(
                        Modifier
                            .widthIn(max = 280.dp)
                            .clip(if (isMe) RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp) else RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp))
                            .background(if (isMe) Color(0xFF075E54) else Color(0xFF1F2C34))
                            .padding(10.dp)
                    ) {
                        Text(m.text, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }

        // INPUT
        Row(Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Ketik pesan...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A3942), unfocusedContainerColor = Color(0xFF2A3942),
                    focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    val txt = input; input = ""
                    scope.launch {
                        db.collection("chats").document(chatId).collection("messages").add(
                            mapOf("senderId" to myUid, "textEnc" to txt, "createdAt" to System.currentTimeMillis())
                        ).await()
                    }
                }
            }) {
                Icon(Icons.Default.Send, "Kirim", tint = Color(0xFF25D366))
            }
        }
    }
}
