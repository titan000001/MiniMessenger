package com.example.minimessenger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var lockOverlay: FrameLayout
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var isLocked = false

    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            var results: Array<Uri>? = null

            if (data?.data != null) {
                // Single file
                results = arrayOf(data.data!!)
            } else if (data?.clipData != null) {
                // Multiple files
                val count = data.clipData!!.itemCount
                results = Array(count) { i -> data.clipData!!.getItemAt(i).uri }
            }

            fileUploadCallback?.onReceiveValue(results)
        } else {
            // Cancelled
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    // Mobile User Agent to ensure we get the mobile site
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        webView = findViewById(R.id.webview)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        lockOverlay = findViewById(R.id.lock_overlay)

        setupWebView()
        setupSwipeRefresh()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val url = intent?.dataString
        if (url != null && (url.contains("facebook.com") || url.contains("messenger.com"))) {
            webView.loadUrl(url)
        } else if (webView.url == null) {
             // Only load default if WebView is empty (first launch)
            webView.loadUrl("https://m.facebook.com/messages/")
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-inject script to apply any changed settings
        injectCustomScript()
        checkAppLock()
    }

    private fun checkAppLock() {
        val prefs = getSharedPreferences("MiniMessengerPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("app_lock", false)) {
             // Only lock if not already authenticated or if we consider moving away as re-lock
             // For simplicity, we lock on every Resume if enabled.
             if (!isLocked) {
                 lockApp()
             }
        } else {
            lockOverlay.visibility = View.GONE
        }
    }

    private fun lockApp() {
        isLocked = true
        lockOverlay.visibility = View.VISIBLE

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isLocked = false
                    lockOverlay.visibility = View.GONE
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels, we must finish the activity to prevent bypass.
                    // For other errors (HW failure), we also finish to fail safe.
                    Toast.makeText(applicationContext, "Authentication failed: $errString", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock MiniMessenger")
            .setSubtitle("Confirm your identity to continue")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        // Only enable swipe to refresh when at the top of the scroll
        // This is a simple implementation; complex web pages might require better scroll detection
        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            swipeRefresh.isEnabled = scrollY == 0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
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

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Cancel existing callback if any
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                // Allow user to choose file type based on accept types if provided
                if (fileChooserParams != null && fileChooserParams.acceptTypes.isNotEmpty()) {
                    // Simple check, in a real app we might map mime types more robustly
                     if (fileChooserParams.acceptTypes.any { it.contains("image") }) {
                         intent.type = "image/*"
                         intent.putExtra(Intent.EXTRA_MIME_TYPES, fileChooserParams.acceptTypes)
                     }
                }

                try {
                    fileChooserLauncher.launch(Intent.createChooser(intent, "Choose File"))
                    return true
                } catch (e: Exception) {
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = null
                    return false
                }
            }
        }

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
                swipeRefresh.isRefreshing = false
                injectCustomScript()
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype))

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, "Downloading File...", Toast.LENGTH_LONG).show()
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
            // 1. Inject the script definitions (only happens once really, but safe to repeat)
            webView.evaluateJavascript(script, null)

            // 2. Read preferences
            val prefs = getSharedPreferences("MiniMessengerPrefs", Context.MODE_PRIVATE)
            val darkMode = prefs.getBoolean("dark_mode", true)
            val hideClutter = prefs.getBoolean("hide_clutter", true)

            // 3. Configure the script
            val configJs = "window.applyMiniMessengerConfig({ darkMode: $darkMode, hideClutter: $hideClutter });"
            webView.evaluateJavascript(configJs, null)
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
