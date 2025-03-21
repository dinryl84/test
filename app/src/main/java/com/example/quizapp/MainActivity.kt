package com.example.quizapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizApp()
        }
    }

    override fun onPause() { // Detect when app is running in background
        super.onPause()
        showWarningDialog(this)
    }

    private fun showWarningDialog(context: Context) { // Warning if switching apps
        AlertDialog.Builder(context)
            .setTitle("Warning")
            .setMessage("Switching apps will cause you to lose all progress. Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // Close the app
            }
            .setNegativeButton("No", null)
            .show()
    }
}

@Composable
fun QuizApp() {
    var quizUrl by remember { mutableStateOf("") }
    var showWebView by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        if (!showWebView) {
            TextField(
                value = quizUrl,
                onValueChange = { quizUrl = it },
                label = { Text("Paste Google Form Link Here") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Button(
                onClick = { if (quizUrl.isNotBlank()) showWebView = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Go")
            }
        } else {
            WebViewScreen(url = quizUrl) { showCompletionDialog(context) }
        }
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
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false
                    }

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

private fun showCompletionDialog(context: Context) { // Show "Well Done" message
    AlertDialog.Builder(context)
        .setTitle("Well Done!")
        .setMessage("Show this to your teacher as proof you finished your quiz.")
        .setPositiveButton("OK") { _, _ -> }
        .show()
}
