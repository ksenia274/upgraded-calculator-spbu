package spbu.wecalc.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn

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

    val scrollState = rememberScrollState()

    var error by remember { mutableStateOf<String?>(null) }

    fun showError(msg: String) { error = msg }

    fun appendToken(token: String) {
        val ops = setOf("+", "-", "*", "/")

        if (token == ".") {
            val t = display.trimEnd()
            if (t.isEmpty() || !t.last().isDigit()) {
                showError("Точку можно ставить только после числа")
                return
            }
            var i = t.length - 1
            while (i >= 0 && (t[i].isDigit() || t[i] == '.')) i--
            val currentNumber = t.substring(i + 1)
            if (currentNumber.contains('.')) {
                showError("В одном числе может быть только 1 точка")
                return
            }
            display = t + "."
            return
        }

        if (token in ops) {
            if (display == "0") {
                if (token == "-") display = "-"
                return
            }

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
        d = d.trimEnd()
        d = if (d.isNotEmpty()) d.dropLast(1).trimEnd() else d
        display = d.ifEmpty { "0" }
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
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black, shape = MaterialTheme.shapes.medium)
                .border(2.dp, Color.White, MaterialTheme.shapes.medium)
                .padding(end = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = display,
                    color = Color.White,
                    softWrap = true,
                    maxLines = Int.MAX_VALUE,
                    style = TextStyle(
                        fontSize = 64.sp,
                        lineHeight = 72.sp,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LaunchedEffect(display) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val buttonHeight = 72.dp
            val verticalGap = 10.dp

            var showHistory by remember { mutableStateOf(false) }

            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(verticalGap)) {
                Button(
                    onClick = {
                        showHistory = !showHistory
                        //TODO: запрос истории из БД
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .border(2.dp, Color.White, MaterialTheme.shapes.medium)
                ) {
                    Text("История вычислений", fontSize = 24.sp)
                }

                AnimatedVisibility(
                    visible = showHistory,
                    enter = expandVertically(animationSpec = tween(220)) + fadeIn(tween(150)),
                    exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(150))
                ) {
                    val historyHeight = buttonHeight * 4 + verticalGap * 3
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(historyHeight)
                            .background(Color.Black, shape = MaterialTheme.shapes.medium)
                            .border(2.dp, Color.White, MaterialTheme.shapes.medium)
                            .padding(12.dp)
                    ) {
                        //TODO:вывести историю вычислений через БД
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            Text(
                                text = "Здесь будет история вычислений",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(verticalGap)) {
                val buttons = listOf(
                    listOf("(", ")", "DEL", "C"),
                    listOf("7", "8", "9", "/"),
                    listOf("4", "5", "6", "*"),
                    listOf("1", "2", "3", "-"),
                    listOf("0", ".", "=", "+")
                )
                buttons.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

    LaunchedEffect(error) {
        if (error != null) {
            delay(3000)
            error = null
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