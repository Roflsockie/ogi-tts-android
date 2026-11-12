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
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
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

    // Метки вида "ru-supertonic-F1" или "voice_m3"
    val styleSuffix = Regex("[-_]([mf])(\\d+)$").find(name)
    if (styleSuffix != null) {
        return if (styleSuffix.groupValues[1] == "m") "male" else "female"
    }

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

// Компактная подпись голоса в стиле "Svetlana - offline - ru"

// Код языка/страны для подписи: en-us -> "us", ru -> "ru"
fun voiceLanguageCode(voice: Voice): String =
    voice.locale.country?.lowercase()?.takeIf { it.isNotBlank() }
        ?: voice.locale.language.lowercase()

// Откуда голос: локально или через сеть
fun voiceSourceTag(voice: Voice): String =
    if (voice.isNetworkConnectionRequired) "online" else "offline"

// Имена для голосов Supertonic (ru-supertonic-F1/M2), по номеру
private val supertonicFemaleNames = listOf("Svetlana", "Anna", "Maria", "Olga", "Daria")
private val supertonicMaleNames = listOf("Ivan", "Pavel", "Sergey", "Dmitri", "Nikolai")

// Читаемое ли имя движка (не внутренний ID вида en-us-x-iom-local)
private fun isReadableVoiceName(name: String): Boolean =
    name.isNotBlank() && !name.contains("-x-") && !name.contains("supertonic-", ignoreCase = true)

// Базовое имя голоса без статуса и языка
fun voiceShortName(voice: Voice, indexInLocale: Int): String {
    Regex("supertonic-([MFmf])([1-9])").find(voice.name)?.let { m ->
        val female = m.groupValues[1].equals("F", ignoreCase = true)
        val names = if (female) supertonicFemaleNames else supertonicMaleNames
        val number = m.groupValues[2].toInt()
        return names.getOrNull(number - 1) ?: "Голос $number"
    }
    // Голос по умолчанию Google: en-US-language / ru-RU-language
    if (voice.name.lowercase().endsWith("-language")) return "Авто"
    if (isReadableVoiceName(voice.name)) return voice.name
    // Google и другие ID без публичных имён: нумеруем внутри локали
    return "Голос ${indexInLocale + 1}"
}

// Ключ пары local/network: один и тот же голос в двух версиях
private fun twinKey(name: String): String =
    name.lowercase().removeSuffix("-network").removeSuffix("-local")

// Убрать сетевые дубли: если у голоса есть offline-версия, online-близнец не нужен
fun dedupeVoiceTwin(voices: List<Voice>): List<Voice> {
    val offlineKeys = voices.filter { !it.isNetworkConnectionRequired }.map { twinKey(it.name) }.toSet()
    return voices.filter { !it.isNetworkConnectionRequired || twinKey(it.name) !in offlineKeys }
}

// Подписи всех голосов: voice.name -> "Svetlana - offline - ru"
fun buildVoiceLabels(voices: Collection<Voice>): Map<String, String> {
    val labels = mutableMapOf<String, String>()
    voices.groupBy { it.locale.toLanguageTag() }.forEach { (_, group) ->
        group.sortedBy { it.name }.forEachIndexed { index, voice ->
            labels[voice.name] =
                "${voiceShortName(voice, index)} - ${voiceSourceTag(voice)} - ${voiceLanguageCode(voice)}"
        }
    }
    return labels
}

// Функция для получения отображаемого имени голоса с полом (запасной формат)
fun getVoiceDisplayName(voice: Voice): String {
    val parts = mutableListOf(voiceLocaleLabel(voice))
    voiceGenderLabel(voice)?.let { parts += it }
    parts += voiceSourceLabel(voice)
    return parts.joinToString(" · ")
}

// Название языка и региона голоса: "English (United States)"
fun voiceLocaleLabel(voice: Voice): String {
    val locale = voice.locale
    val language = Locale.forLanguageTag(locale.language)
        .getDisplayLanguage(Locale.getDefault())
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    val country = locale.displayCountry
    return if (country.isNullOrBlank()) language else "$language ($country)"
}

// Пол голоса в понятном виде, null если определить нельзя
fun voiceGenderLabel(voice: Voice): String? = when (detectVoiceGender(voice.name)) {
    "male" -> "мужской"
    "female" -> "женский"
    else -> null
}

// Откуда голос: локально с устройства или через сеть
fun voiceSourceLabel(voice: Voice): String =
    if (voice.isNetworkConnectionRequired) "сеть" else "офлайн"

// Группа голосов по названию языка
data class VoiceGroup(val label: String, val voices: List<Voice>)

// Группировка голосов по языку, язык текста - первым
fun groupVoicesByLanguage(voices: List<Voice>, preferredLanguage: String): List<VoiceGroup> =
    voices.groupBy { voiceLocaleLabel(it) }
        .map { (label, group) -> VoiceGroup(label, group.sortedBy { it.name }) }
        .sortedWith(
            compareByDescending<VoiceGroup> { group ->
                group.voices.any { it.locale.language == preferredLanguage }
            }.thenBy { it.label }
        )

// Поиск голоса по названию, языку или полу
fun matchVoiceQuery(voice: Voice, query: String): Boolean {
    if (query.isBlank()) return true
    return voice.name.contains(query, ignoreCase = true) ||
        voiceLocaleLabel(voice).contains(query, ignoreCase = true) ||
        (voiceGenderLabel(voice)?.contains(query, ignoreCase = true) == true)
}

// Понятное имя текущего TTS-движка
fun ttsEngineLabel(context: Context): String {
    val pkg = Settings.Secure.getString(context.contentResolver, "tts_default_synth")
    return when (pkg) {
        "com.google.android.tts" -> "Google TTS"
        "com.brahmadeo.supertonic.tts" -> "Supertonic TTS"
        "tts.smartvoice" -> "SmartVoice"
        "com.nekospeak.tts" -> "NekoSpeak"
        "org.woheller69.ttsengine" -> "SherpaTTS"
        null -> "неизвестно"
        else -> pkg
    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(voices: Set<Voice>, tts: TextToSpeech, isSynthesizing: Boolean, onSynthesize: (String) -> Unit, onAudioListRefresh: () -> Unit, ttsReady: Boolean, isPlaying: Boolean) {
    var text by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showAudioList by remember { mutableStateOf(false) }
    var refreshAudioList by remember { mutableStateOf(0) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var voiceQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val voicePrefs = remember { context.getSharedPreferences("ogi_tts_prefs", Context.MODE_PRIVATE) }
    val audioFiles = remember(refreshAudioList) { getAudioFiles(context) }
    val detectedLanguage = remember(text) { detectLanguage(text) }
    val engineLabel = ttsEngineLabel(context)

    // Фильтруем голоса по языку текста
    val filteredVoices = remember(detectedLanguage, voices) {
        filterVoicesByLanguage(voices, detectedLanguage)
    }

    // Группируем голоса по языку (без сетевых дублей), применяем строку поиска
    val displayVoices = remember(voices) { dedupeVoiceTwin(voices.toList()) }
    val groupedVoices = remember(voiceQuery, detectedLanguage, displayVoices) {
        groupVoicesByLanguage(displayVoices.filter { matchVoiceQuery(it, voiceQuery) }, detectedLanguage)
    }

    // Компактные подписи голосов: "Svetlana - offline - ru"
    val voiceLabels = remember(displayVoices) { buildVoiceLabels(displayVoices) }

    // Выбор голоса с сохранением между запусками
    val selectVoice: (Voice) -> Unit = { voice ->
        selectedVoice = voice
        tts.voice = voice
        voicePrefs.edit().putString("voice_name", voice.name).apply()
    }

    // Автоматический выбор голоса на основе языка
    LaunchedEffect(detectedLanguage, voices) {
        if (voices.isNotEmpty() && selectedVoice == null) {
            // Инициализируем голос: сохранённый, иначе подходящий по языку текста
            val savedVoice = voices.find { it.name == voicePrefs.getString("voice_name", null) }
            val defaultVoice = savedVoice
                ?: voices.find { it.locale.language == detectedLanguage }
                ?: voices.find { it.locale.language == "en" }
                ?: voices.first()
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
        color = OgiAbBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Выбор голоса (стиль A): панель, тонкая рамка, акцентная точка
            Box {
                OutlinedTextField(
                    value = selectedVoice?.let { voiceLabels[it.name] ?: getVoiceDisplayName(it) } ?: "Выберите голос",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = OgiAbText),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(OgiAbAccent, CircleShape)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Список голосов",
                            tint = OgiAbTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OgiAbAccent,
                        unfocusedBorderColor = OgiAbBorder,
                        focusedContainerColor = OgiAbPanel,
                        unfocusedContainerColor = OgiAbPanel,
                        focusedTextColor = OgiAbText,
                        unfocusedTextColor = OgiAbText,
                        cursorColor = OgiAbAccent,
                        focusedLeadingIconColor = OgiAbAccent,
                        unfocusedLeadingIconColor = OgiAbAccent
                    )
                )
                // Прозрачная зона клика по всему полю
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showVoiceSheet = true }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Нижний лист со списком голосов: поиск сверху, крупные строки под палец
            if (showVoiceSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showVoiceSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = OgiAbSheet,
                    contentColor = OgiAbText,
                    scrimColor = Color(0x99000000),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFF3F3F46), RoundedCornerShape(2.dp))
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Заголовок листа (стиль A)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Голоса",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = OgiAbText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${displayVoices.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = OgiAbTextFaint
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Поиск по голосам
                        OutlinedTextField(
                            value = voiceQuery,
                            onValueChange = { voiceQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = {
                                Text(
                                    "Поиск: язык или модель",
                                    color = OgiAbTextFaint,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null, tint = OgiAbTextMuted)
                            },
                            trailingIcon = {
                                if (voiceQuery.isNotEmpty()) {
                                    IconButton(onClick = { voiceQuery = "" }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Очистить",
                                            tint = OgiAbTextMuted
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OgiAbAccent,
                                unfocusedBorderColor = OgiAbBorder,
                                focusedContainerColor = OgiAbPanel,
                                unfocusedContainerColor = OgiAbPanel,
                                focusedTextColor = OgiAbText,
                                unfocusedTextColor = OgiAbText,
                                cursorColor = OgiAbAccent,
                                focusedLeadingIconColor = OgiAbTextMuted,
                                unfocusedLeadingIconColor = OgiAbTextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (groupedVoices.isEmpty()) {
                            Text(
                                "Голоса не найдены",
                                color = OgiAbTextMuted,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 460.dp)
                            ) {
                                groupedVoices.forEach { group ->
                                    item(key = "group-${group.label}") {
                                        Text(
                                            "${group.label.uppercase()} — ${group.voices.size}",
                                            color = OgiAbTextFaint,
                                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                                            modifier = Modifier.padding(
                                                start = 20.dp,
                                                end = 20.dp,
                                                top = 14.dp,
                                                bottom = 4.dp
                                            )
                                        )
                                    }
                                    items(group.voices, key = { it.name }) { voice ->
                                        val isSelected = voice.name == selectedVoice?.name
                                        val genderBadge = when (voiceGenderLabel(voice)) {
                                            "мужской" -> "♂"
                                            "женский" -> "♀"
                                            else -> null
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .clickable {
                                                    selectVoice(voice)
                                                    showVoiceSheet = false
                                                }
                                                .padding(start = 8.dp, end = 20.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Акцентная полоска слева у выбранного голоса
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .heightIn(min = 28.dp)
                                                    .background(
                                                        if (isSelected) OgiAbAccent else Color.Transparent
                                                    )
                                            )
                                            Text(
                                                voiceLabels[voice.name] ?: getVoiceDisplayName(voice),
                                                color = if (isSelected) OgiAbAccent else OgiAbText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 16.dp)
                                            )
                                            // Бейдж пола (стиль B)
                                            if (genderBadge != null) {
                                                VoiceBadge(text = genderBadge)
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = OgiAbAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Карточка состояния движка (стиль B) — выше чипов, как в макете
            EngineStatusCard(
                isSynthesizing = isSynthesizing,
                isPlaying = isPlaying,
                ttsReady = ttsReady
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Чипы статуса (стиль A)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(text = "Язык: ${getLanguageName(detectedLanguage)}", accent = true)
                StatusChip(text = "Голосов: ${filteredVoices.size} / ${voices.size}")
                StatusChip(text = engineLabel)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Поле текста (стиль B): плейсхолдер сверху, счётчик — в углу
            val textInteractionSource = remember { MutableInteractionSource() }
            val textFocused by textInteractionSource.collectIsFocusedAsState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OgiAbPanel)
                    .border(
                        1.dp,
                        if (textFocused) OgiAbAccent else OgiAbBorder,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 26.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OgiAbText),
                    cursorBrush = SolidColor(OgiAbAccent),
                    interactionSource = textInteractionSource,
                    enabled = !isLoading && !isSynthesizing,
                    decorationBox = { innerField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (text.isEmpty()) {
                                Text(
                                    "Введите текст для озвучки…",
                                    color = OgiAbTextFaint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )
                            }
                            innerField()
                        }
                    }
                )
                Text(
                    "${text.length} симв.",
                    color = OgiAbTextFaint,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Text(
                    "Загрузка файла…",
                    color = OgiAbTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isSynthesizing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = OgiAbAccent,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Синтез речи…",
                        color = OgiAbTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Нижняя панель действий (стиль A)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Stop — акцентный квадрат
                AppIconButton(
                    icon = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Стоп" else "Прослушать",
                    onClick = {
                        if (isPlaying) {
                            tts.stop()
                            (context as? MainActivity)?.isPlaying = false
                        } else {
                            selectedVoice?.let {
                                val textToSpeak = if (text.isNotBlank()) text else "Введите текст для прослушивания"
                                val params = Bundle().apply {
                                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_playback")
                                }
                                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "tts_playback")
                            }
                        }
                    },
                    accent = true,
                    enabled = selectedVoice != null && ttsReady && !isSynthesizing
                )

                // Импорт файла
                GhostIconButton(
                    icon = Icons.Filled.FileUpload,
                    contentDescription = "Импорт файла",
                    onClick = {
                        filePickerLauncher.launch(arrayOf("text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    }
                )

                // Показать / скрыть аудиофайлы
                GhostIconButton(
                    icon = if (showAudioList) Icons.Filled.AudioFile else Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = if (showAudioList) "Скрыть аудио" else "Показать аудио",
                    onClick = { showAudioList = !showAudioList }
                )

                // Инструкции
                GhostIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = "Инструкции",
                    onClick = { showInstructionsDialog = true }
                )

                // Основное действие
                Button(
                    onClick = {
                        onSynthesize(text)
                        refreshAudioList++
                    },
                    enabled = text.isNotBlank() && selectedVoice != null && !isSynthesizing && ttsReady,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OgiAbAccent,
                        contentColor = Color.Black,
                        disabledContainerColor = OgiAbPanel,
                        disabledContentColor = OgiAbTextFaint
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Озвучить",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Файлы: Downloads/TTS_Audio",
                style = MaterialTheme.typography.labelSmall,
                color = OgiAbTextFaint,
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
            .clip(RoundedCornerShape(8.dp))
            .background(OgiAbPanel)
            .border(1.dp, OgiAbBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка файла
        Icon(
            imageVector = Icons.Filled.AudioFile,
            contentDescription = null,
            tint = OgiAbTextFaint,
            modifier = Modifier.size(16.dp)
        )
        // Время создания (все файлы имеют вид tts_audio_<время>.wav)
        Text(
            text = formatFileTime(file.lastModified()),
            modifier = Modifier.weight(1f),
            color = OgiAbText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Размер файла
        Text(
            text = formatFileSize(file.length()),
            color = OgiAbTextFaint,
            style = MaterialTheme.typography.labelSmall
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
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Стоп" else "Воспроизвести",
                tint = OgiAbAccent
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
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Поделиться",
                tint = OgiAbTextMuted
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
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Удалить",
                tint = OgiAbTextMuted
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

// ---------- Элементы стиля A+B (12.11.2026) ----------

// Чип статуса (стиль A)
@Composable
fun StatusChip(text: String, accent: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (accent) OgiAbAccent.copy(alpha = 0.12f) else OgiAbPanel)
            .border(
                1.dp,
                if (accent) OgiAbAccent.copy(alpha = 0.45f) else OgiAbBorder,
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (accent) OgiAbAccent else OgiAbTextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Карточка состояния движка (стиль B)
@Composable
fun EngineStatusCard(
    isSynthesizing: Boolean,
    isPlaying: Boolean,
    ttsReady: Boolean
) {
    val statusText = when {
        isSynthesizing -> "Синтез речи"
        isPlaying -> "Воспроизведение"
        ttsReady -> "Готов к озвучке"
        else -> "Инициализация движка"
    }
    val statusColor = if (ttsReady || isSynthesizing || isPlaying) OgiAbOk else OgiAbTextMuted
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OgiAbPanelAlt)
            .border(1.dp, OgiAbBorderSoft, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "Состояние движка",
            color = OgiAbTextMuted,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                color = OgiAbText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        EngineWaveform(active = isSynthesizing || isPlaying)
    }
}

// Анимированная волна в карточке состояния
@Composable
fun EngineWaveform(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "engineWave")
    val heights = listOf(8, 15, 22, 11, 24, 17, 9, 19, 13, 7, 16, 21, 12, 8)
    Row(
        modifier = Modifier.height(26.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        heights.forEachIndexed { index, h ->
            val scale by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 620 + index * 70, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "waveBar$index"
            )
            val factor = if (active) scale else 0.35f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((h * factor).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OgiAbAccent.copy(alpha = if (active) 0.9f else 0.45f))
            )
        }
    }
}

// Небольшой бейдж (стиль B)
@Composable
fun VoiceBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(OgiAbPanel)
            .border(1.dp, OgiAbBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = OgiAbTextFaint,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Размер файла для списка: "12 KB", "1.4 MB"
fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

// Время создания файла для списка: "12 ноя 13:25" (коротко, без локали)
private val MONTHS_SHORT = arrayOf(
    "янв", "фев", "мар", "апр", "мая", "июн",
    "июл", "авг", "сен", "окт", "ноя", "дек"
)

fun formatFileTime(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return "${cal.get(Calendar.DAY_OF_MONTH)} ${MONTHS_SHORT[cal.get(Calendar.MONTH)]} " +
        String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

// Кнопка-призрак (стиль A)
@Composable
fun GhostIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    AppIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick
    )
}

// Квадратная кнопка 44dp: акцентная (Play) или с рамкой (призрак)
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    accent: Boolean = false,
    enabled: Boolean = true
) {
    val shape = if (accent) CircleShape else RoundedCornerShape(8.dp)
    val background = when {
        !enabled -> OgiAbBorder
        accent -> OgiAbAccent
        else -> OgiAbPanel
    }
    val tint = when {
        !enabled -> OgiAbTextFaint
        accent -> Color.Black
        else -> OgiAbTextMuted
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(background)
            .then(if (accent || !enabled) Modifier else Modifier.border(1.dp, OgiAbBorder, shape))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
