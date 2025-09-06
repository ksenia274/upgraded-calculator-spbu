package spbu.wecalc.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource

import upgraded_calculator_spbu.composeapp.generated.resources.Res
import upgraded_calculator_spbu.composeapp.generated.resources.compose_multiplatform

@Composable
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFB0B0B0)),
            contentAlignment = Alignment.TopCenter
        ) {
            CalculatorScreen()
        }
    }
}