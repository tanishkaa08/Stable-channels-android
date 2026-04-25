package com.example.ldknodedemo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lightningdevkit.ldknode.Builder
import org.lightningdevkit.ldknode.Network

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) // Sleek Dark Background
                ) {
                    LdkNodeScreen(filesDir.absolutePath)
                }
            }
        }
    }
}

@Composable
fun LdkNodeScreen(storagePath: String) {
    val context = LocalContext.current
    var nodeInfo by remember { mutableStateOf("Booting LDK Node...") }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // Start Node in Background
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val builder = Builder()
                builder.setNetwork(Network.TESTNET)
                builder.setStorageDirPath("$storagePath/ldk_node_data")
                builder.setChainSourceEsplora("https://mempool.space/testnet/api", null)

                val node = builder.build()
                node.start()

                val nodeId = node.nodeId().toString()

                withContext(Dispatchers.Main) {
                    nodeInfo = nodeId
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    nodeInfo = e.message ?: "Unknown Error"
                    isError = true
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡ Stable Channels Demo",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Main Content Area
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFF7931A)) // Bitcoin Orange
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = nodeInfo, color = Color.LightGray, fontSize = 16.sp)
                }
            } else {
                // Card displaying the Node ID
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isError) "❌ Connection Failed" else "✅ Node Initialized",
                            color = if (isError) Color(0xFFE53935) else Color(0xFF4CAF50),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isError) "Error Log:" else "Your Lightning Node ID:",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ID Box
                        Surface(
                            color = Color(0xFF2C2C2C),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nodeInfo,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Copy Button
                        if (!isError) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Node ID", nodeInfo)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Node ID Copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7931A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "COPY NODE ID",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}