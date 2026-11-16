
package com.jarvis.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private val status = mutableStateOf("JARVIS ONLINE")
    private val conversationHistory = mutableListOf<String>()

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                status.value = "MICROPHONE PERMISSION NEEDED"
            }
        }

    private val model by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-3.8-flash")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Firebase.initialize(context = this)

            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            Log.e("JARVIS_FIREBASE_INIT", "Firebase initialization failed", e)
            status.value = "FIREBASE INIT ERROR: ${e.message}"
        }

        textToSpeech = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                textToSpeech?.setSpeechRate(0.92f)
            }
        }

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        status.value = "LISTENING..."
                    }

                    override fun onBeginningOfSpeech() {
                        status.value = "HEARING YOU..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        status.value = "THINKING..."
                    }

                    override fun onError(error: Int) {
                        status.value = "TAP TO TRY AGAIN"
                    }

                    override fun onResults(results: Bundle?) {
                        val spokenText =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )?.firstOrNull()

                        if (!spokenText.isNullOrBlank()) {
                            askJarvis(spokenText)
                        } else {
                            status.value = "TAP TO TRY AGAIN"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )
        }

        setContent {
            MaterialTheme {
                JarvisScreen(
                    status = status.value,
                    onListen = { startListening() }
                )
            }
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.value = "SPEECH NOT AVAILABLE"
            return
        }

        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
            return
        }

        try {
            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )
                putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    1
                )
            }

            status.value = "LISTENING..."
            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            Log.e("JARVIS_SPEECH", "Speech error", e)
            status.value = "MICROPHONE ERROR: ${e.message}"
        }
    }

    private fun askJarvis(question: String) {
        status.value = "THINKING..."

        lifecycleScope.launch {
            try {
                val history = conversationHistory
                    .takeLast(8)
                    .joinToString("\n")

                val prompt = """
                    You are JARVIS, a helpful personal AI assistant.

                    Rules:
                    1. Understand Tamil, Tanglish and English.
                    2. Reply in the same language as the user.
                    3. Explain answers clearly and simply.
                    4. For coding questions, give step-by-step guidance.
                    5. Never invent facts. Say when unsure.
                    6. Keep spoken answers reasonably concise.

                    Previous conversation:
                    $history

                    User: $question

                    JARVIS:
                """.trimIndent()

                val response = model.generateContent(prompt)

                val answer = response.text
                    ?: "Sorry, I could not generate an answer."

                conversationHistory.add("User: $question")
                conversationHistory.add("JARVIS: $answer")

                speakAnswer(answer)

            } catch (e: Exception) {
                Log.e("JARVIS_AI_ERROR", "AI request failed", e)

                val errorDetails =
                    "${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}"

                // Keep the actual error visible on screen.
                status.value = "AI ERROR: $errorDetails"

                // Speak the error without changing the status.
                textToSpeech?.speak(
                    "There was a problem connecting to the AI service.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "JARVIS_ERROR"
                )
            }
        }
    }

    private fun speakAnswer(answer: String) {
        status.value = "SPEAKING..."

        val language = if (
            answer.any { it in '\u0B80'..'\u0BFF' }
        ) {
            Locale("ta", "IN")
        } else {
            Locale.US
        }

        textToSpeech?.let { tts ->
            val result = tts.setLanguage(language)

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.setLanguage(Locale.US)
            }

            tts.speak(
                answer,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_REPLY"
            )
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun JarvisScreen(
    status: String,
    onListen: () -> Unit
) {
    val infiniteTransition =
        rememberInfiniteTransition(label = "jarvis_animation")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020914)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STARK INDUSTRIES",
                color = Color(0xFF42C8FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "J.A.R.V.I.S",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(42.dp))

            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .clickable { onListen() }
            ) {
                val center = this.center
                val baseRadius = size.minDimension * 0.29f

                drawCircle(
                    color = Color(0xFF0077FF).copy(alpha = 0.10f),
                    radius = baseRadius * 1.65f * pulse,
                    center = center
                )

                drawCircle(
                    color = Color(0xFF00BFFF).copy(alpha = 0.18f),
                    radius = baseRadius * 1.35f * pulse,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                rotate(rotation, pivot = center) {
                    drawCircle(
                        color = Color(0xFF48D7FF),
                        radius = baseRadius * 1.18f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                rotate(rotation * -0.7f, pivot = center) {
                    drawCircle(
                        color = Color(0xFF168BFF),
                        radius = baseRadius * 1.02f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                drawCircle(
                    color = Color(0xFF007BFF).copy(alpha = 0.35f),
                    radius = baseRadius * pulse,
                    center = center
                )

                drawCircle(
                    color = Color(0xFF28C9FF),
                    radius = baseRadius * 0.68f,
                    center = center
                )

                drawCircle(
                    color = Color(0xFFEAFBFF),
                    radius = baseRadius * 0.30f,
                    center = center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = status,
                color = Color(0xFF45D5FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TAP THE CORE TO SPEAK",
                color = Color.Gray,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }
    }
}
