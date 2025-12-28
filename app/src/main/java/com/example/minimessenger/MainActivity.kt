package com.example.minimessenger

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Mobile User Agent to ensure we get the mobile site
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        setupWebView()

        // Load Messenger
        webView.loadUrl("https://m.facebook.com/messages/")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString = USER_AGENT
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // Cookie Manager for persistence
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                // Keep everything inside the webview if it matches facebook/messenger
                if (url != null && (url.contains("facebook.com") || url.contains("messenger.com"))) {
                    return false
                }
                // Otherwise let the OS handle it (e.g. external links)
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectCustomScript()
            }
        }
    }

    // Cache the script content to avoid reading from disk on every page load
    private val cachedInjectorScript: String? by lazy {
        try {
            assets.open("js/injector.js").use { inputStream ->
                val buffer = inputStream.readBytes()
                val encoded = android.util.Base64.encodeToString(buffer, android.util.Base64.NO_WRAP)
                "(function() { " +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        "script.innerHTML = window.atob('$encoded');" +
                        "parent.appendChild(script)" +
                        "})()"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun injectCustomScript() {
        cachedInjectorScript?.let { script ->
            webView.evaluateJavascript(script, null)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
