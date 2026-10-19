package app.neono.api

import android.widget.Toast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var user by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }
                if (user == null) {
                    LoginScreen(onLoginSuccess = { user = it })
                } else {
                    HomeScreen(myUid = user!!.uid)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(myUid: String) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1F2C34)) {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text("Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF25D366),
                        selectedTextColor = Color(0xFF25D366),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Timelapse, contentDescription = null) },
                    label = { Text("Pembaruan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF25D366),
                        selectedTextColor = Color(0xFF25D366),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = null) },
                    label = { Text("Music") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF25D366),
                        selectedTextColor = Color(0xFF25D366),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(Icons.Default.Call, contentDescription = null) },
                    label = { Text("Panggilan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF25D366),
                        selectedTextColor = Color(0xFF25D366),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { insets ->
        when (tab) {
            0 -> try {
                    ChatListScreen(myUid = myUid, modifier = Modifier.padding(insets))
                } catch (e: Exception) {
                    val ctx = LocalContext.current
                    Toast.makeText(ctx, "FC: ${e.message}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("ChatListScreen", "Error: ${e.message}", e)
                }
            1 -> ComingSoonScreen("Pembaruan")
            2 -> ComingSoonScreen("Music")
            3 -> ComingSoonScreen("Panggilan")
        }
    }
}

@Composable
fun ComingSoonScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B141A)),
        contentAlignment = Alignment.Center
    ) {
        Text("$title\nComing Soon", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
