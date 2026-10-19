package app.neono.api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatItem(val id: String, val name: String, val lastMessage: String, val time: Long)

@Composable
fun ChatListScreen(myUid: String, modifier: Modifier = Modifier) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    var chats by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // 1. Bikin grup "Neono Official" (kalau belum ada)
        val globalRef = db.collection("chats").document("GLOBAL")
        val globalSnap = globalRef.get().await()
        if (!globalSnap.exists()) {
            globalRef.set(mapOf(
                "type" to "group",
                "name" to "Neono Official",
                "members" to listOf(myUid),
                "isOfficial" to true,
                "lastMessage" to "Selamat datang di Neono!",
                "updatedAt" to System.currentTimeMillis()
            )).await()
        } else {
            // Tambahin user ke grup GLOBAL
            globalRef.update("members", FieldValue.arrayUnion(myUid)).await()
        }

        // 2. Ambil semua chat user
        val snap = db.collection("chats").whereArrayContains("members", myUid).get().await()
        chats = snap.documents.map {
            ChatItem(
                id = it.id,
                name = it.getString("name") ?: "Chat",
                lastMessage = it.getString("lastMessage") ?: "",
                time = it.getLong("updatedAt") ?: 0L
            )
        }.sortedByDescending { it.time }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0B141A))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
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

        // TOMBOL (+) HIJAU
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF25D366)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Chat Baru", tint = Color.White)
        }
    }

    // DIALOG TAMBAH KONTAK
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; errorMsg = null; successMsg = null },
            title = { Text("Tambah Kontak") },
            text = {
                Column {
                    Text("Masukkan ID teman (contoh: NEO-0088)")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        placeholder = { Text("NEO-XXXX") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                    if (successMsg != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(successMsg!!, color = Color(0xFF25D366), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val userRepo = UserRepository()
                        val found = userRepo.findUserByCode(inputCode.trim().uppercase())
                        if (found != null) {
                            val otherUid = found["uid"] as String
                            userRepo.createDirectChat(myUid, otherUid)
                            successMsg = "Kontak telah ditambahkan!"
                            errorMsg = null
                            inputCode = ""
                            // Refresh chat list (nanti)
                        } else {
                            errorMsg = "ID tidak ditemukan"
                            successMsg = null
                        }
                    }
                }) { Text("Cari") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; errorMsg = null }) { Text("Batal") }
            }
        )
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return ""
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = ms
    return "${cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')}"
}
