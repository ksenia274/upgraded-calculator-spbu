package spbu.wecalc.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    fun appendToken(token: String) {
        val ops = setOf("+", "-", "*", "/")

        if (token in ops) {
            if (display == "0") return

            val d = display.trimEnd()
            display = if (d.isNotEmpty() && d.last() in charArrayOf('+','-','*','/')) {
                d.dropLast(1) + token + " "
            } else {
                "$d $token "
            }
            return
        }

        if (display == "0" && token == ".") {
            display = "0."
            return
        }

        display = if (display == "0") token else display + token
    }

    fun backspace() {
        var d = display
        if (d == "0") return
        while (d.isNotEmpty() && d.last().isWhitespace()) d = d.dropLast(1)
        if (d.isNotEmpty()) d = d.dropLast(1)
        while (d.isNotEmpty() && d.last().isWhitespace()) d = d.dropLast(1)
        display = if (d.isEmpty()) "0" else d
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 200.dp, start = 16.dp, end = 16.dp)
            .focusRequester(focusRequester)
            .focusTarget()
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val shift = e.isShiftPressed

                if (shift) {
                    when(e.key) {
                        Key.Equals -> {appendToken("+"); return@onPreviewKeyEvent true}
                        Key.Eight -> {appendToken("*"); return@onPreviewKeyEvent true}
                        Key.Nine -> {appendToken("("); return@onPreviewKeyEvent true}
                        Key.Zero -> {appendToken(")"); return@onPreviewKeyEvent true}
                    }
                }
                when (e.key) {
                    Key.Zero, Key.NumPad0 -> { appendToken("0"); true }
                    Key.One, Key.NumPad1 -> { appendToken("1"); true }
                    Key.Two, Key.NumPad2 -> { appendToken("2"); true }
                    Key.Three, Key.NumPad3 -> { appendToken("3"); true }
                    Key.Four, Key.NumPad4 -> { appendToken("4"); true }
                    Key.Five, Key.NumPad5 -> { appendToken("5"); true }
                    Key.Six, Key.NumPad6 -> { appendToken("6"); true }
                    Key.Seven, Key.NumPad7 -> { appendToken("7"); true }
                    Key.Eight, Key.NumPad8 -> { appendToken("8"); true }
                    Key.Nine, Key.NumPad9 -> { appendToken("9"); true }

                    Key.Minus, Key.NumPadSubtract -> { appendToken("-"); true }
                    Key.Slash, Key.NumPadDivide -> { appendToken("/"); true }
                    Key.NumPadAdd -> { appendToken("+"); true }
                    Key.NumPadMultiply -> { appendToken("*"); true }
                    Key.Period, Key.NumPadDot, Key.Comma -> { appendToken("."); true }

                    Key.Backspace -> { backspace(); true }
                    Key.Delete -> { display = "0"; true }
                    Key.Enter, Key.NumPadEnter -> {
                        //TODO: вызов сервиса(=)
                        true
                    }
                    else -> false
                }
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black, shape = MaterialTheme.shapes.medium)
                .border(2.dp, Color.White, MaterialTheme.shapes.medium)
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = display,
                fontSize = 80.sp,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val buttons = listOf(
                    listOf("(", ")", "DEL", "C"),
                    listOf("7", "8", "9", "/"),
                    listOf("4", "5", "6", "*"),
                    listOf("1", "2", "3", "-"),
                    listOf("0", ".", "=", "+")
                )
                buttons.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { label ->
                            CalculatorButton(label) {
                                when (label) {
                                    "C" -> display = "0"
                                    "DEL" -> backspace()
                                    "=" -> { /* TODO: отправка на сервер */ }
                                    else -> appendToken(label)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun CalculatorButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .size(width = 156.dp, height = 72.dp)
            .padding(4.dp)
            .border(2.dp, Color.White, MaterialTheme.shapes.medium)
    ) {
        Text(label, fontSize = 22.sp)
    }
}