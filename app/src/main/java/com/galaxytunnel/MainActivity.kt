package com.galaxytunnel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URLDecoder

// Data Models
data class Server(
    val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val config: String,
    val pingUrl: String
)

sealed class PingStatus {
    object Checking : PingStatus()
    data class Online(val ms: Long) : PingStatus()
    object Offline : PingStatus()
}

// Localization strings
object Loc {
    val my = mapOf(
        "sidebarHeader" to "Galaxy Tunnel",
        "navServers" to "ဆာဗာများ",
        "navSublink" to "Sublink ထုတ်ရန်",
        "navGuides" to "လမ်းညွှန်များ",
        "navApps" to "ထည့်သွင်းနိုင်သည့် App များ",
        "navSettings" to "Settings",
        "navTunnel" to "Galaxy Tunnel",
        "navContact" to "ဆက်သွယ်ရန်",
        "headerTitle" to "Galaxy Tunnel",
        "serversTitle" to "Galaxy Tunnel",
        "serversSubtitle" to "Secure. Fast. Limitless. — Your Gateway to the Digital World.",
        "settingsTitle" to "Settings",
        "settingsSubtitle" to "စိတ်ကြိုက် ပြင်ဆင်ရန်",
        "themeLabel" to "Theme",
        "lightMode" to "အလင်း",
        "darkMode" to "အမှောင်",
        "sublinkTitle" to "Sublink ထုတ်ရန်",
        "sublinkSubtitle" to "Sub Link ရယူရန် VLESS သို့မဟုတ် Trojan ကို ထည့်ပါ",
        "vlessLabel" to "VLESS/Trojan ကွန်ဖစ် ကို ဤနေရာတွင် ထည့်ပါ:",
        "generateBtn" to "Sub Link ထုတ်မည်",
        "resultLabel" to "ထုတ်ယူရရှိသော Sub Link",
        "copyBtn" to "Copy ကူးယူမည်",
        "guidesTitle" to "လမ်းညွှန်များ",
        "guidesSubtitle" to "အသုံးပြုပုံများကို အောက်တွင် လေ့လာနိုင်ပါသည်။",
        "guide1Title" to "Hiddify အသုံးပြုနည်း",
        "guide1Desc" to "Platforms အားလုံးအတွက် ကြော်ငြာမပါ အကောင်းဆုံး App",
        "guide2Title" to "v2rayNG အသုံးပြုနည်း",
        "guide2Desc" to "Android ဖုန်းများအတွက် အသုံးပြုရခြင်း အဆင်ပြေ အောင်မြင်",
        "guide3Title" to "v2raytun အသုံးပြုနည်း",
        "guide3Desc" to "iOS နှင့် Android အတွက် အဆင်ပြေသော Design ရှိသည့် App",
        "readBtn" to "ဖတ်ရန်",
        "appsTitle" to "ကြိုက်နှစ်သက်ရာ",
        "appsSubtitle" to "အကောင်းဆုံး VPN App များ",
        "app1Desc" to "Platforms အားလုံးအတွက် ကြော်ငြာမပါ အကောင်းဆုံး App",
        "app2Desc" to "Android ဖုန်းများအတွက် အသုံးပြုရခြင်း အဆင်ပြေ အောင်မြင်",
        "app3Desc" to "iOS နှင့် Android အတွက် အဆင်ပြေသော Design ရှိသည့် App",
        "app4Desc" to "Windows, macOS, Linux အတွက် အစွမ်းထက်သော VPN Client",
        "downloadBtn" to "ဒေါင်းလုဒ် ရယူရန်",
        "contactTitle" to "ဆက်သွယ်ရန်",
        "contactSubtitle" to "ကျွန်ုပ်တို့ကို အောက်ပါနည်းလမ်းဖြင့် ဆက်သွယ်နိုင်ပါသည်။",
        "phoneLabel" to "ဖုန်း",
        "addressLabel" to "လိပ်စာ",
        "addressValue" to "ကရင်ပြည်နယ်၊ မြဝတီမြို့နယ်၊ သင်္ကန်းညီနောင်",
        "mapBtn" to "Google Map တွင်ကြည့်ရန်",
        "serverSelectBtn" to "ရွေးချယ်ရန်",
        "checking" to "စစ်ဆေးနေသည်...",
        "offline" to "လိုင်းမရှိပါ",
        "copied" to "ကူးယူပြီးပါပြီ",
        "navImport" to "Configs တင်သွင်းရန်",
        "importTitle" to "Configs တင်သွင်းမှု",
        "importSubtitle" to "သင့်ကိုယ်ပိုင် VLESS သို့မဟုတ် Trojan config ကို ထည့်သွင်းပါ",
        "importLabel" to "ကွန်ဖစ်ကုဒ်လိုင်း (Config Link) ထည့်ရန်:",
        "importPlaceholder" to "ဤနေရာတွင် paste လုပ်ပါ...",
        "importNameLabel" to "ဆာဗာအမည် (မဖြစ်မနေမဟုတ်ပါ):",
        "importBtn" to "တင်သွင်းမည်",
        "importDeleteBtn" to "ဖျက်မည်",
        "importNoData" to "ကိုယ်ပိုင်တင်သွင်းထားသော config မရှိသေးပါ။",
        "importClearAll" to "အားလုံးဖျက်မည်",
        "invalidConfig" to "မှန်ကန်သော vless သို့မဟုတ် trojan config မဟုတ်ပါ။",
        "importSuccess" to "ဆာဗာကို အောင်မြင်စွာ တင်သွင်းပြီးပါပြီ။"
    )

    val en = mapOf(
        "sidebarHeader" to "Galaxy Tunnel",
        "navServers" to "Servers",
        "navSublink" to "Generate Sublink",
        "navGuides" to "Guides",
        "navApps" to "Download Apps",
        "navSettings" to "Settings",
        "navTunnel" to "Galaxy Tunnel",
        "navContact" to "Contact Us",
        "headerTitle" to "Galaxy Tunnel",
        "serversTitle" to "Galaxy Tunnel",
        "serversSubtitle" to "Secure. Fast. Limitless. — Your Gateway to the Digital World.",
        "settingsTitle" to "Settings",
        "settingsSubtitle" to "Customize your experience",
        "themeLabel" to "Theme",
        "lightMode" to "Light",
        "darkMode" to "Dark",
        "sublinkTitle" to "Generate Sublink",
        "sublinkSubtitle" to "Enter VLESS/Trojan config to get Sub Link",
        "vlessLabel" to "Paste Config Here:",
        "generateBtn" to "Generate Sub Link",
        "resultLabel" to "Generated Sub Link",
        "copyBtn" to "Copy Sub Link",
        "guidesTitle" to "Guides",
        "guidesSubtitle" to "Learn how to use our services below.",
        "guide1Title" to "How to use Hiddify",
        "guide1Desc" to "Best ad-free app for all platforms",
        "guide2Title" to "How to use v2rayNG",
        "guide2Desc" to "Reliable and easy to use for Android",
        "guide3Title" to "How to use v2raytun",
        "guide3Desc" to "Stylish app for both iOS and Android",
        "readBtn" to "Read More",
        "appsTitle" to "Favorites",
        "appsSubtitle" to "Best VPN Apps Selection",
        "app1Desc" to "Best ad-free app for all platforms",
        "app2Desc" to "Reliable and easy to use for Android",
        "app3Desc" to "Stylish app for both iOS and Android",
        "app4Desc" to "Powerful VPN client for Windows, macOS, Linux",
        "downloadBtn" to "Download Now",
        "contactTitle" to "Contact Us",
        "contactSubtitle" to "You can reach us through the following methods.",
        "phoneLabel" to "Phone",
        "addressLabel" to "Address",
        "addressValue" to "Kayin State, Myawaddy Township, Thin Gun Nyi Naung",
        "mapBtn" to "View on Google Map",
        "serverSelectBtn" to "Select Server",
        "checking" to "Checking...",
        "offline" to "Offline",
        "copied" to "Copied!",
        "navImport" to "Import Configs",
        "importTitle" to "Import Configurations",
        "importSubtitle" to "Import and manage your own custom VLESS or Trojan servers",
        "importLabel" to "Config Link (vless:// or trojan://):",
        "importPlaceholder" to "Paste config link here...",
        "importNameLabel" to "Server Name (Optional):",
        "importBtn" to "Import Server",
        "importDeleteBtn" to "Delete",
        "importNoData" to "No custom configurations imported yet.",
        "importClearAll" to "Clear All",
        "invalidConfig" to "Invalid VLESS or Trojan configuration link.",
        "importSuccess" to "Server imported successfully!"
    )

    fun get(key: String, lang: String): String {
        return if (lang == "my") {
            my[key] ?: en[key] ?: key
        } else {
            en[key] ?: key
        }
    }
}

class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var database: AppDatabase
    private lateinit var repository: ImportedServerRepository

    // Default Fallback Servers
    private val defaultServers = listOf(
        Server(
            id = 1,
            name = "SERVER 01 (VLESS Galaxy)",
            description = "Premium High Speed Node",
            location = "Multi-Region",
            config = "vless://26fe5cdd-e772-4238-8adc-9bf53d4781fa@coca.nobless.workers.dev:443?path=%2F&security=tls&encryption=none&host=coca.nobless.workers.dev&type=ws&sni=coca.nobless.workers.dev#Galaxy-Tunnel",
            pingUrl = "coca.nobless.workers.dev"
        ),
        Server(
            id = 2,
            name = "SERVER 02 (VLESS Univers)",
            description = "High Speed - Premium",
            location = "Global CDN",
            config = "vless://8221a740-8218-4775-ab45-0bab948285ec@sub.galaxytunnel2026.workers.dev:443?security=tls&encryption=none&host=sub.galaxytunnel2026.workers.dev&type=ws&sni=sub.galaxytunnel2026.workers.dev#Galaxy-Tunnel",
            pingUrl = "sub.galaxytunnel2026.workers.dev"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        database = AppDatabase.getDatabase(this)
        repository = ImportedServerRepository(database.importedServerDao())

        try {
            dev.dev7.lib.v2ray.V2rayController.init(this, R.mipmap.ic_launcher, "Galaxy Tunnel")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            var language by remember { mutableStateOf(getSavedString("lang", "my")) }
            var theme by remember { mutableStateOf(getSavedString("theme", "light")) }
            var textSize by remember { mutableStateOf(getSavedString("textSize", "medium")) }
            var iconSize by remember { mutableStateOf(getSavedString("iconSize", "medium")) }
            var killSwitch by remember { mutableStateOf(getSavedBoolean("killSwitch", false)) }
            var udpRelay by remember { mutableStateOf(getSavedBoolean("udpRelay", false)) }
            var eyeComfort by remember { mutableStateOf(getSavedBoolean("eyeComfort", false)) }
            var selectedServerId by remember { mutableStateOf<Int?>(getSavedInt("selectedServerId", -1).let { if (it == -1) null else it }) }

            var onlineServers by remember { mutableStateOf(defaultServers) }
            val customServers by repository.allServers.collectAsState(initial = emptyList())

            val servers = remember(onlineServers, customServers) {
                val list = onlineServers.toMutableList()
                customServers.forEach { custom ->
                    if (list.none { it.config == custom.config }) {
                        val id = 10000 + custom.id
                        val afterAt = custom.config.substringAfter('@', "")
                        val hostPart = afterAt.substringBefore(':', "google.com").substringBefore('/', "google.com")
                        val pingUrl = if (hostPart.isNotEmpty()) hostPart else "google.com"
                        list.add(
                            Server(
                                id = id,
                                name = custom.name.ifEmpty { "Imported Server" },
                                description = "User Config Node",
                                location = "Custom Import",
                                config = custom.config,
                                pingUrl = pingUrl
                            )
                        )
                    }
                }
                list
            }

            var vpnState by remember { mutableStateOf(try { dev.dev7.lib.v2ray.V2rayController.getConnectionState()?.name ?: "DISCONNECTED" } catch(e: Exception) { "DISCONNECTED" }) }
            var duration by remember { mutableStateOf("00:00:00") }
            var uploadSpeed by remember { mutableStateOf("0 KB/s") }
            var downloadSpeed by remember { mutableStateOf("0 KB/s") }
            val isConnected = vpnState == "CONNECTED" || vpnState == "CONNECTING"

            val context = LocalContext.current
            DisposableEffect(context) {
                val receiver = object : android.content.BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        if (intent != null) {
                            val state = intent.getSerializableExtra("CONNECTION_STATE_EXTRA")
                            if (state != null) {
                                vpnState = state.toString()
                            }
                            duration = intent.getStringExtra("SERVICE_DURATION_EXTRA") ?: "00:00:00"
                            uploadSpeed = intent.getStringExtra("UPLOAD_SPEED_EXTRA") ?: "0 KB/s"
                            downloadSpeed = intent.getStringExtra("DOWNLOAD_SPEED_EXTRA") ?: "0 KB/s"
                        }
                    }
                }
                val filter = android.content.IntentFilter("V2RAY_SERVICE_STATICS_INTENT")
                androidx.core.content.ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    androidx.core.content.ContextCompat.RECEIVER_EXPORTED
                )
                onDispose {
                    try {
                        context.unregisterReceiver(receiver)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            var pings by remember { mutableStateOf<Map<Int, PingStatus>>(emptyMap()) }
            var isLoadingServers by remember { mutableStateOf(true) }

            val coroutineScope = rememberCoroutineScope()

            // Save state functions
            val onLanguageChange: (String) -> Unit = {
                language = it
                saveString("lang", it)
            }
            val onThemeChange: (String) -> Unit = {
                theme = it
                saveString("theme", it)
            }
            val onTextSizeChange: (String) -> Unit = {
                textSize = it
                saveString("textSize", it)
            }
            val onIconSizeChange: (String) -> Unit = {
                iconSize = it
                saveString("iconSize", it)
            }
            val onKillSwitchChange: (Boolean) -> Unit = {
                killSwitch = it
                saveBoolean("killSwitch", it)
            }
            val onUdpRelayChange: (Boolean) -> Unit = {
                udpRelay = it
                saveBoolean("udpRelay", it)
            }
            val onEyeComfortChange: (Boolean) -> Unit = {
                eyeComfort = it
                saveBoolean("eyeComfort", it)
            }
            val onSelectedServerChange: (Int?) -> Unit = {
                selectedServerId = it
                saveInt("selectedServerId", it ?: -1)
            }

            // Fetch online servers once at start
            LaunchedEffect(Unit) {
                isLoadingServers = true
                fetchServersList(
                    onSuccess = { fetched ->
                        onlineServers = fetched
                        isLoadingServers = false
                    },
                    onFailure = {
                        onlineServers = defaultServers
                        isLoadingServers = false
                    }
                )
            }

            // Ping all servers whenever servers change
            LaunchedEffect(servers) {
                if (servers.isNotEmpty()) {
                    pingAllServers(servers, coroutineScope) { id, status ->
                        pings = pings + (id to status)
                    }
                }
            }

            AppTheme(theme = theme, textSize = textSize) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainScreen(
                            language = language,
                            theme = theme,
                            textSize = textSize,
                            iconSize = iconSize,
                            killSwitch = killSwitch,
                            udpRelay = udpRelay,
                            eyeComfort = eyeComfort,
                            selectedServerId = selectedServerId,
                            servers = servers,
                            customServers = customServers,
                            isConnected = isConnected,
                            vpnState = vpnState,
                            duration = duration,
                            uploadSpeed = uploadSpeed,
                            downloadSpeed = downloadSpeed,
                            pings = pings,
                            isLoadingServers = isLoadingServers,
                            onLanguageChange = onLanguageChange,
                            onThemeChange = onThemeChange,
                            onTextSizeChange = onTextSizeChange,
                            onIconSizeChange = onIconSizeChange,
                            onKillSwitchChange = onKillSwitchChange,
                            onUdpRelayChange = onUdpRelayChange,
                            onEyeComfortChange = onEyeComfortChange,
                            onSelectedServerChange = onSelectedServerChange,
                            onConnectedChange = { start ->
                                if (start) {
                                    val activeServer = servers.find { it.id == selectedServerId }
                                    if (activeServer != null) {
                                        try {
                                            dev.dev7.lib.v2ray.V2rayController.startV2ray(
                                                this@MainActivity,
                                                activeServer.name,
                                                activeServer.config,
                                                ArrayList<String>()
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    dev.dev7.lib.v2ray.V2rayController.stopV2ray(this@MainActivity)
                                }
                            },
                            onImportServer = { name, config ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    repository.insert(ImportedServer(name = name, config = config))
                                }
                            },
                            onDeleteServer = { id ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    repository.delete(id)
                                }
                            },
                            onClearAllServers = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    repository.clearAll()
                                }
                            }
                        )

                        // Eye Comfort Amber Overlay (Only in Light mode)
                        if (theme == "light" && eyeComfort) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.10f))
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper functions for SharedPreferences persistence
    private fun saveString(key: String, value: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    private fun getSavedString(key: String, default: String): String {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return default
        return sharedPref.getString(key, default) ?: default
    }

    private fun saveBoolean(key: String, value: Boolean) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    private fun getSavedBoolean(key: String, default: Boolean): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return default
        return sharedPref.getBoolean(key, default)
    }

    private fun saveInt(key: String, value: Int) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    private fun getSavedInt(key: String, default: Int): Int {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return default
        return sharedPref.getInt(key, default)
    }

    // Fetch config and parse servers
    private fun fetchServersList(onSuccess: (List<Server>) -> Unit, onFailure: (Exception) -> Unit) {
        val url = "https://raw.githubusercontent.com/Galaxy-Tunnel/ONE-AGENT/refs/heads/main/configs.txt"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        runOnUiThread { onFailure(IOException("Unexpected HTTP response: $response")) }
                        return
                    }
                    val body = response.body?.string() ?: ""
                    val servers = parseConfigsText(body)
                    runOnUiThread { onSuccess(servers) }
                } catch (e: Exception) {
                    runOnUiThread { onFailure(e) }
                }
            }
        })
    }

    private fun parseConfigsText(text: String): List<Server> {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return defaultServers

        return lines.mapIndexed { index, line ->
            val namePart = line.substringAfter('#', "")
            val name = if (namePart.isNotEmpty()) {
                try {
                    URLDecoder.decode(namePart, "UTF-8")
                } catch (e: Exception) {
                    namePart
                }
            } else {
                "Server ${index + 1}"
            }

            // Extract host name for socket ping
            val afterAt = line.substringAfter('@', "")
            val hostPart = afterAt.substringBefore(':', "google.com").substringBefore('/', "google.com")
            val pingUrl = if (hostPart.isNotEmpty()) hostPart else "google.com"

            Server(
                id = index + 1,
                name = name,
                description = "High Speed Node",
                location = "Multi-Region",
                config = line,
                pingUrl = pingUrl
            )
        }
    }

    // Async socket ping
    private fun pingAllServers(
        serversList: List<Server>,
        scope: CoroutineScope,
        onPingUpdated: (Int, PingStatus) -> Unit
    ) {
        serversList.forEach { server ->
            scope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    onPingUpdated(server.id, PingStatus.Checking)
                }
                val host = server.pingUrl
                val port = 443
                val startTime = System.currentTimeMillis()
                var isSuccess = false
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(host, port), 2500)
                    socket.close()
                    isSuccess = true
                } catch (e: Exception) {
                    // Try fallback port 80
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(host, 80), 1500)
                        socket.close()
                        isSuccess = true
                    } catch (e2: Exception) {
                        // Keep offline
                    }
                }
                val duration = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        onPingUpdated(server.id, PingStatus.Online(duration))
                    } else {
                        onPingUpdated(server.id, PingStatus.Offline)
                    }
                }
            }
        }
    }
}

// Custom Jetpack Compose App Theme
@Composable
fun AppTheme(
    theme: String,
    textSize: String,
    content: @Composable () -> Unit
) {
    val isDark = theme == "dark"

    val colors = if (isDark) {
        darkColorScheme(
            primary = Color(0xFF2563EB), // Rich Blue
            onPrimary = Color.White,
            background = Color(0xFF121212), // Deep Zinc
            surface = Color(0xFF1E1E1E), // Zinc card
            onBackground = Color(0xFFF5F5F4),
            onSurface = Color(0xFFF5F5F4),
            outline = Color(0xFF2E2E2E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF2563EB),
            onPrimary = Color.White,
            background = Color(0xFFF5F5F4), // Clean Stone
            surface = Color.White,
            onBackground = Color(0xFF1C1917),
            onSurface = Color(0xFF1C1917),
            outline = Color(0xFFE7E5E4)
        )
    }

    val baseScale = when (textSize) {
        "small" -> 0.85f
        "large" -> 1.2f
        else -> 1.0f
    }

    val typography = Typography(
        headlineMedium = LocalTextStyle(fontSize = 24.sp, scale = baseScale, fontWeight = FontWeight.Bold),
        titleLarge = LocalTextStyle(fontSize = 20.sp, scale = baseScale, fontWeight = FontWeight.SemiBold),
        bodyLarge = LocalTextStyle(fontSize = 16.sp, scale = baseScale, fontWeight = FontWeight.Normal),
        bodyMedium = LocalTextStyle(fontSize = 14.sp, scale = baseScale, fontWeight = FontWeight.Normal),
        labelMedium = LocalTextStyle(fontSize = 12.sp, scale = baseScale, fontWeight = FontWeight.Bold)
    )

    val view = androidx.compose.ui.platform.LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}

private fun LocalTextStyle(fontSize: TextUnit, scale: Float, fontWeight: FontWeight): TextStyle {
    return TextStyle(
        fontSize = (fontSize.value * scale).sp,
        fontWeight = fontWeight,
        fontFamily = FontFamily.SansSerif
    )
}

// Main Scaffold and Navigation Controller
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    language: String,
    theme: String,
    textSize: String,
    iconSize: String,
    killSwitch: Boolean,
    udpRelay: Boolean,
    eyeComfort: Boolean,
    selectedServerId: Int?,
    servers: List<Server>,
    customServers: List<ImportedServer>,
    isConnected: Boolean,
    vpnState: String,
    duration: String,
    uploadSpeed: String,
    downloadSpeed: String,
    pings: Map<Int, PingStatus>,
    isLoadingServers: Boolean,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onTextSizeChange: (String) -> Unit,
    onIconSizeChange: (String) -> Unit,
    onKillSwitchChange: (Boolean) -> Unit,
    onUdpRelayChange: (Boolean) -> Unit,
    onEyeComfortChange: (Boolean) -> Unit,
    onSelectedServerChange: (Int?) -> Unit,
    onConnectedChange: (Boolean) -> Unit,
    onImportServer: (String, String) -> Unit,
    onDeleteServer: (Int) -> Unit,
    onClearAllServers: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf("tunnel") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                SidebarContent(
                    language = language,
                    currentPage = currentPage,
                    killSwitch = killSwitch,
                    udpRelay = udpRelay,
                    onNavigate = {
                        currentPage = it
                        coroutineScope.launch { drawerState.close() }
                    },
                    onKillSwitchChange = onKillSwitchChange,
                    onUdpRelayChange = onUdpRelayChange
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                HeaderBar(
                    language = language,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onLanguageSelect = onLanguageChange
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentPage) {
                    "tunnel" -> {
                        TunnelScreen(
                            language = language,
                            theme = theme,
                            iconSize = iconSize,
                            eyeComfort = eyeComfort,
                            selectedServerId = selectedServerId,
                            servers = servers,
                            isConnected = isConnected,
                            vpnState = vpnState,
                            duration = duration,
                            uploadSpeed = uploadSpeed,
                            downloadSpeed = downloadSpeed,
                            pings = pings,
                            isLoadingServers = isLoadingServers,
                            onEyeComfortChange = onEyeComfortChange,
                            onSelectedServerChange = onSelectedServerChange,
                            onConnectedChange = onConnectedChange
                        )
                    }
                    "settings" -> {
                        SettingsScreen(
                            language = language,
                            theme = theme,
                            textSize = textSize,
                            iconSize = iconSize,
                            onThemeChange = onThemeChange,
                            onTextSizeChange = onTextSizeChange,
                            onIconSizeChange = onIconSizeChange
                        )
                    }
                    "import_configs" -> {
                        ImportConfigsScreen(
                            language = language,
                            importedServers = customServers,
                            onImportServer = onImportServer,
                            onDeleteServer = onDeleteServer,
                            onClearAll = onClearAllServers
                        )
                    }
                }
            }
        }
    }
}

// Header Composable with Navigation Hamburger and Language selection
@Composable
fun HeaderBar(
    language: String,
    onMenuClick: () -> Unit,
    onLanguageSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Rocket Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LanguageButton(
                    label = "MY",
                    isSelected = language == "my",
                    onClick = { onLanguageSelect("my") }
                )
                LanguageButton(
                    label = "EN",
                    isSelected = language == "en",
                    onClick = { onLanguageSelect("en") }
                )
            }
        }
    }
}

@Composable
fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

// Sidebar Drawer Composable
@Composable
fun SidebarContent(
    language: String,
    currentPage: String,
    killSwitch: Boolean,
    udpRelay: Boolean,
    onNavigate: (String) -> Unit,
    onKillSwitchChange: (Boolean) -> Unit,
    onUdpRelayChange: (Boolean) -> Unit
) {
    val t = Loc

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Logo / Title area
        Text(
            text = t.get("sidebarHeader", language),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 16.dp))

        // Navigation Menu items
        NavigationDrawerItem(
            label = t.get("navTunnel", language),
            icon = Icons.Default.Shield,
            isSelected = currentPage == "tunnel",
            onClick = { onNavigate("tunnel") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = t.get("navImport", language),
            icon = Icons.Default.CloudUpload,
            isSelected = currentPage == "import_configs",
            onClick = { onNavigate("import_configs") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = t.get("navSettings", language),
            icon = Icons.Default.Settings,
            isSelected = currentPage == "settings",
            onClick = { onNavigate("settings") }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 16.dp))

        // Advanced Settings
        Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kill Switch",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = killSwitch,
                onCheckedChange = onKillSwitchChange
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UDP Relay",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = udpRelay,
                onCheckedChange = onUdpRelayChange
            )
        }
    }
}

@Composable
fun NavigationDrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Galaxy Tunnel Main Screen Composable
@Composable
fun TunnelScreen(
    language: String,
    theme: String,
    iconSize: String,
    eyeComfort: Boolean,
    selectedServerId: Int?,
    servers: List<Server>,
    isConnected: Boolean,
    vpnState: String,
    duration: String,
    uploadSpeed: String,
    downloadSpeed: String,
    pings: Map<Int, PingStatus>,
    isLoadingServers: Boolean,
    onEyeComfortChange: (Boolean) -> Unit,
    onSelectedServerChange: (Int?) -> Unit,
    onConnectedChange: (Boolean) -> Unit
) {
    val t = Loc
    val selectedServer = servers.find { it.id == selectedServerId }

    // Icon Density sizing
    val iconScale = when (iconSize) {
        "small" -> 0.85f
        "large" -> 1.2f
        else -> 1.0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- FIXED TOP PORTION ---
        
        // Logo / Title
        Logo()

        // Connection Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PowerButton(
                    isConnected = isConnected,
                    enabled = selectedServerId != null,
                    iconScale = iconScale,
                    onClick = {
                        if (selectedServerId != null) {
                            onConnectedChange(!isConnected)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (vpnState) {
                        "CONNECTED" -> if (language == "my") "ချိတ်ဆက်ထားသည်" else "Connected"
                        "CONNECTING" -> if (language == "my") "ချိတ်ဆက်နေသည်..." else "Connecting..."
                        else -> if (language == "my") "ချိတ်ဆက်ရန်" else "Disconnected"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (vpnState) {
                        "CONNECTED" -> Color(0xFF10B981) // Green
                        "CONNECTING" -> Color(0xFFF59E0B) // Amber
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                )

                if (vpnState == "CONNECTED" || vpnState == "CONNECTING") {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Duration display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duration",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speeds side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Download Speed
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download Speed",
                                tint = Color(0xFF10B981), // Green
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = downloadSpeed,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Upload Speed
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upload Speed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uploadSpeed,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                selectedServer?.let { server ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Eye Comfort Control (only shows in Light Mode)
        if (theme == "light") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eye Comfort",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { onEyeComfortChange(!eyeComfort) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (eyeComfort) Color(0xFFF59E0B) else Color(0xFFE7E5E4),
                            contentColor = if (eyeComfort) Color.White else Color(0xFF1C1917)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (eyeComfort) "ON" else "OFF",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // --- SCROLLABLE BOTTOM PORTION ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Server List items
            if (isLoadingServers) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                items(servers) { server ->
                    val isSelected = selectedServerId == server.id
                    val pingStatus = pings[server.id] ?: PingStatus.Checking

                    val cardBorderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }

                    val cardBorderWidth = if (isSelected) 2.dp else 1.dp

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(cardBorderWidth, cardBorderColor),
                        tonalElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = server.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = server.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Ping state representation
                                when (pingStatus) {
                                    is PingStatus.Checking -> {
                                        Text(
                                            text = t.get("checking", language),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                    is PingStatus.Offline -> {
                                        Text(
                                            text = t.get("offline", language),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    is PingStatus.Online -> {
                                        val ms = pingStatus.ms
                                        val pingColor = when {
                                            ms < 400 -> Color(0xFF10B981) // Emerald
                                            ms < 800 -> Color(0xFF06B6D4) // Cyan
                                            else -> Color(0xFFF59E0B) // Amber
                                        }
                                        Text(
                                            text = "$ms ms",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = pingColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Select button
                                Button(
                                    onClick = { onSelectedServerChange(server.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.background
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) {
                                            if (language == "my") "ရွေးချယ်ထားသည်" else "Selected"
                                        } else {
                                            t.get("serverSelectBtn", language)
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Power Button widget with pulsate animations
@Composable
fun PowerButton(
    isConnected: Boolean,
    enabled: Boolean,
    iconScale: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color(0xFFE4E4E7) // Zinc gray
            isConnected -> Color(0xFF10B981) // Connected green
            else -> Color(0xFFF43F5E) // Disconnected red
        }
    )

    val iconColor = if (!enabled) Color.Gray else Color.White

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(buttonColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PowerSettingsNew,
            contentDescription = "Power",
            tint = iconColor,
            modifier = Modifier
                .size(40.dp)
                .scale(iconScale)
        )
    }
}

// Custom Premium Gradient Header Logo Canvas
@Composable
fun Logo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GALAXY TUNNEL",
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2563EB), // Premium Blue
                        Color(0xFF67E8F9)  // Vibrant Cyan
                    )
                ),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        )
    }
}

// Settings Screen Composable
@Composable
fun SettingsScreen(
    language: String,
    theme: String,
    textSize: String,
    iconSize: String,
    onThemeChange: (String) -> Unit,
    onTextSizeChange: (String) -> Unit,
    onIconSizeChange: (String) -> Unit
) {
    val t = Loc

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = t.get("settingsTitle", language),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t.get("settingsSubtitle", language),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Theme Options Group
        SettingsCard(
            title = t.get("themeLabel", language),
            icon = Icons.Default.Palette
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsSelectButton(
                    label = t.get("lightMode", language),
                    icon = Icons.Default.LightMode,
                    isSelected = theme == "light",
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeChange("light") }
                )
                SettingsSelectButton(
                    label = t.get("darkMode", language),
                    icon = Icons.Default.DarkMode,
                    isSelected = theme == "dark",
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeChange("dark") }
                )
            }
        }

        // Font Size Options Group
        SettingsCard(
            title = "Font Size",
            icon = Icons.Default.FormatSize
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("small", "medium", "large").forEach { size ->
                    SettingsSelectButton(
                        label = size.uppercase(),
                        isSelected = textSize == size,
                        modifier = Modifier.weight(1f),
                        onClick = { onTextSizeChange(size) }
                    )
                }
            }
        }

        // Icon Density Options Group
        SettingsCard(
            title = "Icon Density",
            icon = Icons.Default.Face
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("small", "medium", "large").forEach { size ->
                    SettingsSelectButton(
                        label = size.uppercase(),
                        isSelected = iconSize == size,
                        modifier = Modifier.weight(1f),
                        onClick = { onIconSizeChange(size) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsSelectButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center
        )
        if (isSelected && icon == null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// Import Configurations Screen
@Composable
fun ImportConfigsScreen(
    language: String,
    importedServers: List<com.galaxytunnel.ImportedServer>,
    onImportServer: (name: String, config: String) -> Unit,
    onDeleteServer: (id: Int) -> Unit,
    onClearAll: () -> Unit
) {
    val t = Loc
    var configText by remember { mutableStateOf("") }
    var serverName by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = t.get("importTitle", language),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t.get("importSubtitle", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Add Config Input Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = t.get("importLabel", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = configText,
                        onValueChange = { configText = it },
                        placeholder = { Text(t.get("importPlaceholder", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = t.get("importNameLabel", language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        placeholder = { Text("e.g. My Premium Server") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            val trimmedConfig = configText.trim()
                            if (trimmedConfig.startsWith("vless://") || trimmedConfig.startsWith("trojan://")) {
                                onImportServer(serverName.trim(), trimmedConfig)
                                configText = ""
                                serverName = ""
                                Toast.makeText(context, t.get("importSuccess", language), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, t.get("invalidConfig", language), Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t.get("importBtn", language),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // List Header with Clear All Button
        if (importedServers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Imported (${importedServers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onClearAll) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = t.get("importClearAll", language), color = Color.Red)
                    }
                }
            }

            items(importedServers) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name.ifEmpty { "Imported Server" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.config,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeleteServer(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = t.get("importDeleteBtn", language),
                                tint = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = t.get("importNoData", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
