package com.example.quizapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    private var backgroundRunCount = 0 // Track how many times app goes to background

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizApp()
        }
    }

    override fun onPause() { // Detect app running in background
        super.onPause()
        backgroundRunCount++
        if (backgroundRunCount == 1) {
            showFirstWarningDialog(this)
        } else if (backgroundRunCount == 2) {
            clearWebViewCookies()
            finishAffinity() // Close the app after second background run
        }
    }

    private fun showFirstWarningDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Warning")
            .setMessage("Switching apps may cause you to lose all progress. If you do it again, the quiz will be reset.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun clearWebViewCookies() { // Clears form data before closing
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }
}

@Composable
fun QuizApp() {
    var quizUrl by remember { mutableStateOf("") }
    var showWebView by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title and Subtitle
        Text("QuizApp", fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text("Made by a teacher for the teachers and students", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

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
        .setMessage("Show this to your teacher as proof that you have completed your quiz")
        .setPositiveButton("OK") { _, _ -> }
        .show()
}
