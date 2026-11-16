package com.jarvis.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisScreen()
        }
    }
}

@Composable
fun JarvisScreen() {
    var status by remember { mutableStateOf("J.A.R.V.I.S ONLINE") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020814)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF0066CC),
                                Color(0xFF020814)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "J",
                    color = Color.White,
                    fontSize = 75.sp
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                status,
                color = Color(0xFF00E5FF),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "IRON MAN AI ASSISTANT",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
