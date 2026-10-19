package app.neono.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ChatItem(val id: String, val name: String, val lastMessage: String, val time: Long)

@Composable
fun ChatListScreen(myUid: String, modifier: Modifier = Modifier) {
    val db = remember { FirebaseFirestore.getInstance() }
    var chats by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Ambil chat yang user ikuti
        val snap = db.collection("chats").whereArrayContains("members", myUid).get().await()
        chats = snap.documents.map {
            ChatItem(
                id = it.id,
                name = it.getString("name") ?: "Chat",
                lastMessage = it.getString("lastMessage") ?: "",
                time = it.getLong("updatedAt") ?: 0L
            )
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0B141A))) {
        // TOP BAR (dengan titik tiga)
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Neono", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Profil") }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Pengaturan") }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Privasi") }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Keluar") }, onClick = { showMenu = false })
                }
            }
        }

        // LIST CHAT
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(chats.size) { i ->
                val chat = chats[i]
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { /* Buka chat */ }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF2A3942)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(chat.name.take(1).uppercase(), color = Color.White, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(chat.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(formatTime(chat.time), color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return ""
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = ms
    return "${cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')}"
}
