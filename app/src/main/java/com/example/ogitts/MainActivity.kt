package com.example.ogitts

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.tooling.preview.Preview
import com.example.ogitts.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.InputStream
import java.util.*

fun detectLanguage(text: String): String {
    val ukrainianChars = setOf('ї', 'є', 'і', 'ґ', 'Ї', 'Є', 'І', 'Ґ')
    val cyrillicCount = text.count { it.isLetter() && it in '\u0400'..'\u04FF' }
    val latinCount = text.count { it.isLetter() && it in 'a'..'z' || it in 'A'..'Z' }
    val japaneseCount = text.count { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FFF' }

    return when {
        cyrillicCount > 0 -> {
            val ukrainianSpecific = text.count { it in ukrainianChars }
            if (ukrainianSpecific > 0) "uk" else "ru"
        }
        japaneseCount > 0 -> "ja"
        latinCount > 0 -> "en"
        else -> "en" // default
    }
}

fun getLanguageName(code: String): String {
    return when (code) {
        "uk" -> "Ukrainian"
        "ru" -> "Russian"
        "en" -> "English"
        "ja" -> "Japanese"
        else -> "Unknown"
    }
}

// Функция для определения пола голоса
fun detectVoiceGender(voiceName: String): String {
    val name = voiceName.lowercase()

    // Проверяем явные указания пола
    if (name.contains("female") || name.contains("woman") || name.contains("girl")) {
        return "female"
    }
    if (name.contains("male") || name.contains("man") || name.contains("boy")) {
        return "male"
    }

    // Проверяем распространенные паттерны в названиях голосов
    val femalePatterns = listOf(
        "alice", "emma", "lisa", "mary", "anna", "sara", "linda", "nancy", "amy", "karen",
        "laura", "susan", "betty", "helen", "rachel", "olivia", "sophia", "ava", "mia", "charlotte",
        "amelia", "harper", "evelyn", "abigail", "emily", "elizabeth", "sofia", "grace", "chloe",
        "victoria", "lily", "hannah", "zoey", "natalie", "addison", "aubrey", "lillian", "brooklyn",
        "ellie", "stella", "savannah", "leah", "zoe", "audrey", "gabriella", "bella", "skylar",
        "allison", "samantha", "madison", "avery", "riley", "sydney", "alexandra", "maya",
        "melanie", "madelyn", "mackenzie", "sadie", "makayla", "sienna", "adeline", "peyton",
        "mackenzie", "sadie", "makayla", "sienna", "adeline", "peyton"
    )

    val malePatterns = listOf(
        "david", "michael", "james", "robert", "john", "william", "richard", "joseph", "thomas",
        "charles", "daniel", "matthew", "anthony", "mark", "donald", "steven", "paul", "andrew",
        "joshua", "kenneth", "kevin", "brian", "george", "timothy", "ronald", "jason", "edward",
        "jacob", "gary", "nicholas", "eric", "jonathan", "stephen", "larry", "justin", "scott",
        "brandon", "benjamin", "samuel", "gregory", "alexander", "patrick", "franklin", "raymond",
        "jack", "dennis", "jerry", "tyler", "aaron", "jose", "adam", "nathan", "henry", "douglas",
        "zachary", "peter", "kyle", "ethan", "walter", "noah", "christopher", "luke", "logan",
        "carter", "jackson", "owen", "gabriel", "grayson", "levi", "theodore", "jayden", "lucas",
        "asher", "mason", "jaxon", "mateo", "oliver", "elijah", "liam", "william", "bennett",
        "hudson", "jack", "leo", "nolan", "ryan", "cameron", "dylan", "ezra", "luca", "miles"
    )

    // Проверяем по паттернам
    for (pattern in femalePatterns) {
        if (name.contains(pattern)) {
            return "female"
        }
    }

    for (pattern in malePatterns) {
        if (name.contains(pattern)) {
            return "male"
        }
    }

    // Если не удалось определить, возвращаем "unknown"
    return "unknown"
}

// Функция для фильтрации голосов по языку
fun filterVoicesByLanguage(voices: Set<Voice>, languageCode: String): List<Voice> {
    return voices.filter { voice ->
        voice.locale.language == languageCode
    }.sortedBy { it.name }
}

// Функция для получения отображаемого имени голоса с полом
fun getVoiceDisplayName(voice: Voice): String {
    val gender = detectVoiceGender(voice.name)
    val genderText = when (gender) {
        "male" -> "\"male\""
        "female" -> "\"female\""
        else -> "\"unknown\""
    }
    return "${voice.name} $genderText"
}

fun readFileContent(context: Context, uri: Uri): String {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val mimeType = context.contentResolver.getType(uri)
            when (mimeType) {
                "text/plain" -> stream.readBytes().toString(Charsets.UTF_8)
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                    val document = XWPFDocument(stream)
                    document.paragraphs.joinToString("\n") { it.text }
                }
                else -> "Неподдерживаемый формат файла"
            }
        } ?: "Ошибка чтения файла"
    } catch (e: SecurityException) {
        "Ошибка доступа к файлу: ${e.message}"
    } catch (e: Exception) {
        "Ошибка чтения файла: ${e.message}"
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private var voices: Set<Voice> = emptySet()
    var isSynthesizing by mutableStateOf(false)
    private var ttsReady by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)

    // Вспомогательная функция для показа Toast сообщений
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем папку TTS_Audio в Downloads
        val appAudioDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "TTS_Audio")
        if (!appAudioDir.exists()) {
            appAudioDir.mkdirs()
        }
        
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                voices = tts.voices ?: emptySet()
                ttsReady = true
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        when (utteranceId) {
                            "tts_synthesis" -> isSynthesizing = true
                            "tts_playback" -> isPlaying = true
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        when (utteranceId) {
                            "tts_synthesis" -> {
                                isSynthesizing = false
                                showToast("Аудио сохранено")
                            }
                            "tts_playback" -> {
                                isPlaying = false
                            }
                        }
                    }
                    @Deprecated("Deprecated in API level 18")
                    override fun onError(utteranceId: String?) {
                        when (utteranceId) {
                            "tts_synthesis" -> {
                                isSynthesizing = false
                                showToast("Ошибка синтеза")
                            }
                            "tts_playback" -> {
                                isPlaying = false
                            }
                        }
                    }
                })
            } else {
                showToast("Ошибка инициализации TTS")
            }
        }
        setContent {
            OGITTSAndroidTheme(darkTheme = true) {
                var permissionsGranted by remember { mutableStateOf(checkPermissions()) }

                if (permissionsGranted) {
                    MainScreen(voices = voices, tts = tts, isSynthesizing = isSynthesizing, onSynthesize = { text ->
                        synthesizeToFile(text)
                    }, onAudioListRefresh = {
                        // Обновление списка аудио файлов будет реализовано через состояние
                    }, ttsReady = ttsReady, isPlaying = isPlaying)
                } else {
                    WelcomeScreen(onPermissionsGranted = { permissionsGranted = true })
                }
            }
        }
    }

    private fun synthesizeToFile(text: String) {
        if (text.isNotBlank()) {
            try {
                val fileName = "tts_audio_${System.currentTimeMillis()}.wav"
                val appAudioDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "TTS_Audio")
                val file = File(appAudioDir, fileName)

                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_synthesis")
                }

                val result = tts.synthesizeToFile(text, params, file, "tts_synthesis")
                if (result == TextToSpeech.ERROR) {
                    showToast("Ошибка синтеза речи")
                    isSynthesizing = false
                } else {
                    showToast("Начинаем синтез...")
                }
            } catch (e: Exception) {
                showToast("Ошибка: ${e.message}")
                isSynthesizing = false
            }
        } else {
            showToast("Текст пустой")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    fun openAudioFolder(context: Context) {
        try {
            val appAudioDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "TTS_Audio")
            if (appAudioDir.exists()) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(appAudioDir), "resource/folder")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    showToast("Папка: ${appAudioDir.absolutePath}")
                }
            } else {
                showToast("Папка TTS_Audio не найдена")
            }
        } catch (e: Exception) {
            showToast("Ошибка: ${e.message}")
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 (API 29)
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            // Android 9 and below
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun WelcomeScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
            Toast.makeText(context, "Permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissions denied!", Toast.LENGTH_SHORT).show()
        }
    }

    val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        // Android 10 (API 29)
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } else {
        // Android 9 and below
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // Градиентный фон
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            OgiBackgroundDark, // #121212
            OgiSurfaceDark     // #1E1E1E
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OgiBackgroundDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Анимированный логотип с звуковыми волнами
                Box(
                    modifier = Modifier.padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Основной логотип (можно заменить на изображение)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                color = OgiPrimaryDarkTheme,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "OGI TTS Logo",
                            modifier = Modifier.size(80.dp),
                            tint = Color.Black
                        )
                    }

                    // Анимированные звуковые волны
                    // Левая сторона
                    Box(
                        modifier = Modifier
                            .offset(x = (-48).dp, y = 0.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        // Волна 1
                        Box(
                            modifier = Modifier
                                .size(4.dp, 16.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                        )
                        // Волна 2
                        Box(
                            modifier = Modifier
                                .size(4.dp, 24.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                                .offset(x = (-8).dp)
                        )
                        // Волна 3
                        Box(
                            modifier = Modifier
                                .size(4.dp, 12.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                                .offset(x = (-16).dp)
                        )
                    }

                    // Правая сторона
                    Box(
                        modifier = Modifier
                            .offset(x = 48.dp, y = 0.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        // Волна 1
                        Box(
                            modifier = Modifier
                                .size(4.dp, 12.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                        )
                        // Волна 2
                        Box(
                            modifier = Modifier
                                .size(4.dp, 24.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                                .offset(x = 8.dp)
                        )
                        // Волна 3
                        Box(
                            modifier = Modifier
                                .size(4.dp, 16.dp)
                                .background(OgiPrimaryDarkTheme, RoundedCornerShape(2.dp))
                                .align(Alignment.Center)
                                .offset(x = 16.dp)
                        )
                    }
                }

                // Заголовок
                Text(
                    text = "Welcome to OGI TTS Android!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OgiTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    lineHeight = 40.sp
                )

                // Описание
                Text(
                    text = "The application needs access to files for importing texts and saving audio. Please grant the permissions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OgiTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp),
                    lineHeight = 24.sp
                )

                // Кнопка с улучшенным дизайном
                Button(
                    onClick = {
                        Toast.makeText(context, "Requesting permissions...", Toast.LENGTH_SHORT).show()
                        launcher.launch(permissions)
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OgiPrimaryDarkTheme,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(voices: Set<Voice>, tts: TextToSpeech, isSynthesizing: Boolean, onSynthesize: (String) -> Unit, onAudioListRefresh: () -> Unit, ttsReady: Boolean, isPlaying: Boolean) {
    var text by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showAudioList by remember { mutableStateOf(false) }
    var refreshAudioList by remember { mutableStateOf(0) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audioFiles = remember(refreshAudioList) { getAudioFiles(context) }
    val detectedLanguage = remember(text) { detectLanguage(text) }

    // Фильтруем голоса по языку текста
    val filteredVoices = remember(detectedLanguage, voices) {
        filterVoicesByLanguage(voices, detectedLanguage)
    }

    // Автоматический выбор голоса на основе языка
    LaunchedEffect(detectedLanguage, voices) {
        if (voices.isNotEmpty() && selectedVoice == null) {
            // Инициализируем голос при первом запуске
            val defaultVoice = voices.find { it.locale.language == "en" } ?: voices.first()
            selectedVoice = defaultVoice
            tts.voice = defaultVoice
        }
        if (filteredVoices.isNotEmpty()) {
            // Если есть голоса для текущего языка, выбираем первый
            val preferredVoice = filteredVoices.firstOrNull()
            if (preferredVoice != null && selectedVoice?.locale?.language != detectedLanguage) {
                selectedVoice = preferredVoice
                tts.voice = preferredVoice
            }
        } else if (detectedLanguage != "en") {
            // Если нет голосов для языка, показать предупреждение
            Toast.makeText(context, "Нет голосов для языка ${getLanguageName(detectedLanguage)}, используются английские голоса", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            CoroutineScope(Dispatchers.IO).launch {
                val content = readFileContent(context, it)
                withContext(Dispatchers.Main) {
                    text = content
                    isLoading = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OgiBackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Выбор голоса с улучшенным дизайном
            Box {
                OutlinedTextField(
                    value = if (selectedVoice != null) getVoiceDisplayName(selectedVoice!!) else "Select voice",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = OgiTextSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OgiPrimaryDarkTheme,
                        unfocusedBorderColor = OgiTextSecondary,
                        focusedTextColor = OgiTextPrimary,
                        unfocusedTextColor = OgiTextPrimary,
                        cursorColor = OgiPrimaryDarkTheme
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(OgiSurfaceDark)
                ) {
                    // Показываем отфильтрованные голоса для текущего языка
                    if (filteredVoices.isNotEmpty()) {
                        filteredVoices.forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        getVoiceDisplayName(voice),
                                        color = OgiTextPrimary
                                    )
                                },
                                onClick = {
                                    selectedVoice = voice
                                    tts.voice = voice
                                    expanded = false
                                }
                            )
                        }
                    } else {
                        // Если нет голосов для языка, показываем все доступные голоса
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No voices for ${getLanguageName(detectedLanguage)}, showing all voices:",
                                    color = OgiTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            onClick = { /* Не кликабельно */ }
                        )
                        voices.sortedBy { it.name }.forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        getVoiceDisplayName(voice),
                                        color = OgiTextPrimary
                                    )
                                },
                                onClick = {
                                    selectedVoice = voice
                                    tts.voice = voice
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Статус-индикаторы с цветовой кодировкой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Detected language: ${getLanguageName(detectedLanguage)}",
                    color = OgiSecondaryDarkTheme,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Available voices: ${filteredVoices.size}",
                    color = if (filteredVoices.isNotEmpty()) OgiSuccess else OgiTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TTS Status: ${if (ttsReady) "Ready" else "Initializing..."}",
                    color = if (ttsReady) OgiSuccess else OgiTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Selected: ${selectedVoice?.let { detectVoiceGender(it.name) } ?: "none"}",
                    color = OgiTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Улучшенная панель управления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OgiSurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Play/Stop
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            tts.stop()
                            (context as? MainActivity)?.isPlaying = false
                        } else {
                            selectedVoice?.let {
                                val textToSpeak = if (text.isNotBlank()) text else "Enter text to listen"
                                val params = Bundle().apply {
                                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_playback")
                                }
                                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "tts_playback")
                            }
                        }
                    },
                    enabled = selectedVoice != null && ttsReady && !isSynthesizing,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (selectedVoice != null && ttsReady && !isSynthesizing)
                                OgiPrimaryDarkTheme else OgiTextSecondary,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        tint = Color.Black
                    )
                }

                // Кнопка импорта файла
                IconButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(OgiPrimaryDarkTheme, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileUpload,
                        contentDescription = "Import file",
                        tint = Color.Black
                    )
                }

                // Кнопка показать/скрыть аудио
                IconButton(
                    onClick = { showAudioList = !showAudioList },
                    modifier = Modifier
                        .size(48.dp)
                        .background(OgiPrimaryDarkTheme, CircleShape)
                ) {
                    Icon(
                        imageVector = if (showAudioList) Icons.Filled.AudioFile else Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = if (showAudioList) "Hide audio" else "Show audio",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка инструкций
                IconButton(
                    onClick = {
                        // Показать диалог с инструкциями
                        showInstructionsDialog = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(OgiPrimaryDarkTheme, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Instructions",
                        tint = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text to synthesize", color = OgiTextSecondary) },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && !isSynthesizing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OgiPrimaryDarkTheme,
                    unfocusedBorderColor = OgiTextSecondary,
                    focusedTextColor = OgiTextPrimary,
                    unfocusedTextColor = OgiTextPrimary,
                    cursorColor = OgiPrimaryDarkTheme,
                    focusedContainerColor = OgiSurfaceDark,
                    unfocusedContainerColor = OgiSurfaceDark
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Text("Loading file...", color = OgiTextSecondary)
            }
            if (isSynthesizing) {
                CircularProgressIndicator(color = OgiPrimaryDarkTheme)
                Text("Synthesizing speech...", color = OgiTextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Нижняя панель с основными действиями
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Export (экспорт аудио)
                Button(
                    onClick = {
                        onSynthesize(text)
                        refreshAudioList++
                    },
                    enabled = text.isNotBlank() && selectedVoice != null && !isSynthesizing && ttsReady,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OgiPrimaryDarkTheme,
                        contentColor = Color.Black,
                        disabledContainerColor = OgiTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Export",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Audio")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Files saved to: Downloads/TTS_Audio",
                style = MaterialTheme.typography.bodySmall,
                color = OgiTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (showAudioList) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(audioFiles) { file ->
                        AudioFileItem(file = file, context = context, onRefresh = { refreshAudioList++ })
                    }
                }
            }

            // Диалог с инструкциями
            if (showInstructionsDialog) {
                AlertDialog(
                    onDismissRequest = { showInstructionsDialog = false },
                    title = {
                        Text(
                            "Instructions / Инструкции",
                            color = OgiTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "OGI TTS Android - Text-to-Speech Application",
                                color = OgiTextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "Приложение для преобразования текста в речь",
                                color = OgiTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "English Instructions:",
                                color = OgiTextPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "1. Enter text or import file - language is auto-detected\n" +
                                "2. Voices are automatically filtered by detected language\n" +
                                "3. Gender indicators: ♂ male, ♀ female, ⚲ unknown\n" +
                                "4. Select voice from filtered list for best results\n" +
                                "5. Press Play to test voice, Export to save audio\n" +
                                "6. Status shows available voices and selected gender\n" +
                                "7. Manage saved audio files using the Audio List toggle",
                                color = OgiTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Русские инструкции:",
                                color = OgiTextPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "1. Введите текст или импортируйте файл - язык определяется автоматически\n" +
                                "2. Голоса автоматически фильтруются по обнаруженному языку\n" +
                                "3. Индикаторы пола: ♂ мужской, ♀ женский, ⚲ неизвестный\n" +
                                "4. Выберите голос из отфильтрованного списка для лучших результатов\n" +
                                "5. Нажмите \"Воспроизведение\" для теста, \"Экспорт\" для сохранения\n" +
                                "6. Статус показывает доступные голоса и выбранный пол\n" +
                                "7. Управляйте сохранёнными аудиофайлами с помощью переключателя списка аудио",
                                color = OgiTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showInstructionsDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OgiPrimaryDarkTheme,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("OK")
                        }
                    },
                    containerColor = OgiSurfaceDark,
                    titleContentColor = OgiTextPrimary,
                    textContentColor = OgiTextSecondary
                )
            }
        }
    }
}

@Composable
fun AudioFileItem(file: File, context: Context, onRefresh: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File name with improved styling
        Text(
            text = file.name,
            modifier = Modifier.weight(1f),
            color = OgiTextPrimary,
            style = MaterialTheme.typography.bodyLarge
        )

        // Play/Stop Button - Blue (#64B5F6)
        IconButton(
            onClick = {
                try {
                    if (isPlaying) {
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        isPlaying = false
                    } else {
                        mediaPlayer.setDataSource(context, Uri.fromFile(file))
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        isPlaying = true
                        mediaPlayer.setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                } catch (e: Exception) {
                    isPlaying = false
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(OgiPrimaryDarkTheme, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                tint = Color.Black
            )
        }

        // Share Button - Teal (#26A69A)
        IconButton(
            onClick = {
                // Share файл
                try {
                    if (!file.exists()) {
                        Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }

                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "com.example.ogitts.fileprovider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/wav"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_TITLE, file.name)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Проверяем, есть ли приложения для обработки
                    val packageManager = context.packageManager
                    val activities = packageManager.queryIntentActivities(shareIntent, 0)

                    if (activities.isNotEmpty()) {
                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться аудиофайлом"))
                    } else {
                        Toast.makeText(context, "Нет приложений для отправки файлов", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка: ${e.localizedMessage ?: e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(OgiSecondaryDarkTheme, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = Color.White
            )
        }

        // Delete Button - Red (#D32F2F)
        IconButton(
            onClick = {
                // Удалить файл
                try {
                    if (file.exists()) {
                        val deleted = file.delete()
                        if (deleted) {
                            Toast.makeText(context, "Файл удален", Toast.LENGTH_SHORT).show()
                            onRefresh()
                        } else {
                            Toast.makeText(context, "Не удалось удалить файл", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка удаления: ${e.localizedMessage ?: e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(OgiError, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

internal fun getAudioFiles(context: Context): List<File> {
    val appAudioDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "TTS_Audio")
    return appAudioDir.listFiles()?.filter { it.extension == "wav" && it.name.startsWith("tts_audio_") }?.toList() ?: emptyList()
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OGITTSAndroidTheme {
        Greeting("Android")
    }
}
