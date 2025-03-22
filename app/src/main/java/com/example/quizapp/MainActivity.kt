package com.example.quizapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizApp(this)
        }
    }
}

@Composable
fun QuizApp(context: Context) {
    var quizUrl by remember { mutableStateOf("") }
    var showWebView by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(0L) }
    var showWarning by remember { mutableStateOf(false) }
    var warningDismissed by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(remainingTime) {
        if (remainingTime in 1..60_000 && !warningDismissed) {
            showWarning = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QuizApp", fontSize = 24.sp)
        Text("Made by a teacher for teachers and students", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (!showWebView) {
            Text("Select Quiz Duration", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                listOf(listOf(2, 10, 20), listOf(60)).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        row.forEach { minutes ->
                            Button(
                                onClick = {
                                    remainingTime = (minutes * 60 * 1000).toLong()
                                    warningDismissed = false
                                    startTimer(context, remainingTime) { timeLeft ->
                                        remainingTime = timeLeft
                                        if (timeLeft <= 0) {
                                            clearWebViewCookies()
                                            (context as? ComponentActivity)?.finishAffinity()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("${minutes} min")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (remainingTime > 0) {
                TextField(
                    value = quizUrl,
                    onValueChange = { quizUrl = it },
                    label = { Text("Paste Google Form Link Here") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
                Button(onClick = { if (quizUrl.isNotBlank()) showWebView = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Start Quiz")
                }
            }
        } else {
            WebViewScreen(url = quizUrl) { showCompletionDialog = true }
        }
    }

    if (showWarning) {
        WarningOverlay {
            showWarning = false
            warningDismissed = true
        }
    }

    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Well Done!") },
            text = { Text("Show this to your teacher as proof that you have completed your quiz") },
            confirmButton = {
                Button(onClick = { showCompletionDialog = false }) { Text("OK") }
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, onQuizCompleted: () -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (url?.contains("formResponse") == true) {
                            onQuizCompleted()
                        }
                    }
                }
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun WarningOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️ TIME IS RUNNING OUT! ⚠️", fontSize = 30.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Less than a minute left! Submit your quiz now!", fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss) { Text("OK", fontSize = 20.sp) }
        }
    }
}

private fun startTimer(context: Context, duration: Long, onTick: (Long) -> Unit) {
    object : CountDownTimer(duration, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            onTick(millisUntilFinished)
        }
        override fun onFinish() {
            clearWebViewCookies()
            (context as? ComponentActivity)?.finishAffinity()
        }
    }.start()
}

private fun clearWebViewCookies() {
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
}
