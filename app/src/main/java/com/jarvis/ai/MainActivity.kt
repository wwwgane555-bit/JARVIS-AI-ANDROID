
package com.jarvis.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(2, 8, 20)
        window.navigationBarColor = android.graphics.Color.rgb(2, 8, 20)

        setContent {
            JarvisScreen()
        }
    }
}

@Composable
fun JarvisScreen() {

    val context = LocalContext.current

    var status by remember { mutableStateOf("JARVIS ONLINE") }
    var spokenText by remember { mutableStateOf("Tap the orb and speak") }
    var permissionGranted by remember { mutableStateOf(false) }

    var recognizer by remember {
        mutableStateOf<SpeechRecognizer?>(null)
    }

    var tts by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    val cyan = Color(0xFF00E5FF)

    val pulse = rememberInfiniteTransition(label = "pulse")
        .animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbPulse"
        )

    val rotation = rememberInfiniteTransition(label = "rotation")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = LinearEasing)
            ),
            label = "orbRotation"
        )

    DisposableEffect(Unit) {

        if (SpeechRecognizer.isRecognitionAvailable(context)) {

            recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            recognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        status = "LISTENING..."
                    }

                    override fun onBeginningOfSpeech() {
                        status = "HEARING YOU"
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        status = "THINKING..."
                    }

                    override fun onError(error: Int) {
                        status = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "VOICE NOT CLEAR"

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "NO SPEECH DETECTED"

                            SpeechRecognizer.ERROR_NETWORK ->
                                "NETWORK ERROR"

                            else -> "VOICE ERROR: $error"
                        }
                    }

                    override fun onResults(results: Bundle?) {

                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                        val result = matches?.firstOrNull()

                        if (!result.isNullOrBlank()) {

                            spokenText = result
                            status = "SPEAKING..."

                            val reply = "You said: $result"

                            tts?.speak(
                                reply,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "JARVIS_REPLY"
                            )
                        } else {
                            status = "NO VOICE DETECTED"
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )

        } else {
            status = "SPEECH RECOGNITION NOT AVAILABLE"
        }

        tts = TextToSpeech(context) { result ->

            if (result == TextToSpeech.SUCCESS) {

                val tamilResult = tts?.setLanguage(
                    Locale("ta", "IN")
                )

                if (
                    tamilResult == TextToSpeech.LANG_MISSING_DATA ||
                    tamilResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.language = Locale("en", "IN")
                }

                tts?.setSpeechRate(0.92f)
                tts?.setPitch(1.0f)
            }
        }

        onDispose {
            recognizer?.destroy()
            recognizer = null

            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            permissionGranted = granted

            if (granted) {
                startListening(recognizer)
            } else {
                status = "MICROPHONE PERMISSION DENIED"
            }
        }

    fun onOrbTap() {

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            permissionGranted = true
            startListening(recognizer)
        } else {
            permissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF06182B),
                        Color(0xFF020814),
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "STARK INDUSTRIES",
                color = cyan.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Canvas(
                modifier = Modifier
                    .size(290.dp)
                    .clickable { onOrbTap() }
            ) {

                val centerPoint = center
                val radius = size.minDimension * 0.29f * pulse.value

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cyan.copy(alpha = 0.35f),
                            Color(0xFF0066FF).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = centerPoint,
                        radius = radius * 2.2f
                    ),
                    radius = radius * 2.2f,
                    center = centerPoint
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            cyan,
                            Color(0xFF0066FF),
                            Color(0xFF00152B)
                        ),
                        center = centerPoint,
                        radius = radius
                    ),
                    radius = radius,
                    center = centerPoint
                )

                drawCircle(
                    color = cyan,
                    radius = radius * 1.12f,
                    center = centerPoint,
                    style = Stroke(width = 3.dp.toPx())
                )

                rotate(rotation.value, pivot = centerPoint) {

                    drawArc(
                        color = Color.White,
                        startAngle = 20f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = Offset(
                            centerPoint.x - radius * 1.35f,
                            centerPoint.y - radius * 1.35f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            radius * 2.7f,
                            radius * 2.7f
                        ),
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    drawArc(
                        color = cyan,
                        startAngle = 190f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(
                            centerPoint.x - radius * 1.5f,
                            centerPoint.y - radius * 1.5f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            radius * 3f,
                            radius * 3f
                        ),
                        style = Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = radius * 0.09f,
                    center = centerPoint
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                "J.A.R.V.I.S",
                color = cyan,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                status,
                color = Color(0xFF00FF9D),
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                spokenText,
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 25.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "TAP THE ENERGY CORE TO SPEAK",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

private fun startListening(
    recognizer: SpeechRecognizer?
) {
    if (recognizer == null) return

    val intent = Intent(
        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
    ).apply {

        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ta-IN"
        )

        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "ta-IN"
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

    try {
        recognizer.startListening(intent)
    } catch (e: Exception) {
        // Recognition could not start.
    }
}
