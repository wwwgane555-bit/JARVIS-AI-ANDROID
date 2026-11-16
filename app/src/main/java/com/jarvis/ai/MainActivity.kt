
package com.jarvis.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    val transition = rememberInfiniteTransition(label = "Jarvis")

    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val cyan = Color(0xFF00E5FF)
    val blue = Color(0xFF0066FF)
    val background = Color(0xFF020814)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF06182B),
                        background,
                        Color(0xFF00030A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "STARK INDUSTRIES",
                color = cyan.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(35.dp))

            Canvas(
                modifier = Modifier.size(290.dp)
            ) {

                val centerPoint = center
                val radius = size.minDimension * 0.29f * pulse

                // Outer energy glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cyan.copy(alpha = 0.35f),
                            blue.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = centerPoint,
                        radius = radius * 2.2f
                    ),
                    radius = radius * 2.2f,
                    center = centerPoint
                )

                // Main glowing core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            cyan,
                            blue,
                            Color(0xFF00152B)
                        ),
                        center = centerPoint,
                        radius = radius
                    ),
                    radius = radius,
                    center = centerPoint
                )

                // Inner ring
                drawCircle(
                    color = cyan.copy(alpha = 0.9f),
                    radius = radius * 1.12f,
                    center = centerPoint,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Rotating energy rings
                rotate(rotation, pivot = centerPoint) {

                    drawArc(
                        color = Color.White,
                        startAngle = 20f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = Offset(
                            centerPoint.x - radius * 1.35f,
                            centerPoint.y - radius * 1.35f
                        ),
                        size = Size(
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
                        size = Size(
                            radius * 3f,
                            radius * 3f
                        ),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Center light
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.09f,
                    center = centerPoint
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "J.A.R.V.I.S",
                color = cyan,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "● SYSTEM ONLINE",
                color = Color(0xFF00FF9D),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PERSONAL AI ASSISTANT",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
        }
    }
}
