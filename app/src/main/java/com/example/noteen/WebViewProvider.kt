package com.example.noteen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("StaticFieldLeak")
object TextEditorEngine {
    private lateinit var _webView: WebView
    val webView: WebView
        get() = _webView

    private val _buttonStatesJson = mutableStateOf("{}")
    val buttonStatesJson: State<String> = _buttonStatesJson

    private val _undoStatesJson = mutableStateOf("{}")
    val undoStatesJson: State<String> = _undoStatesJson

    private val _shouldShowToolbar = mutableStateOf(false)
    val shouldShowToolbar: State<Boolean> = _shouldShowToolbar

    fun setToolbarVisibility(visible: Boolean) {
        _shouldShowToolbar.value = visible
    }

    private val _jsonContent = mutableStateOf("")
    val jsonContent: State<String> = _jsonContent

    private val _plainTextContent = mutableStateOf("")
    val plainTextContent: State<String> = _plainTextContent

    private val _titleString = mutableStateOf("")
    val titleString: State<String> = _titleString

    fun reset() {
        _shouldShowToolbar.value = false
        _titleString.value = ""
        _jsonContent.value = ""
        _plainTextContent.value = ""
    }

    fun init(context: Context) {
        if (::_webView.isInitialized) return

        val appContext = context.applicationContext

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(appContext))
            .build()


        _webView = WebView(appContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x00000000)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    val url = request?.url ?: return null

                    return when {
                        url.host == "myapp.local" && url.encodedPath?.startsWith("/external/") == true -> {
                            val filename = url.lastPathSegment ?: return null
                            val file = File(appContext.getExternalFilesDir("images"), filename)

                            if (!file.exists()) {
                                null
                            } else {
                                val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "image/*"
                                val inputStream = FileInputStream(file)
                                WebResourceResponse(mimeType, "utf-8", inputStream)
                            }
                        }

                        url.scheme == "content" -> {
                            try {
                                val mimeType = appContext.contentResolver.getType(url)
                                    ?: URLConnection.guessContentTypeFromName(url.lastPathSegment)
                                    ?: "application/octet-stream"

                                val inputStream = appContext.contentResolver.openInputStream(url)
                                    ?: return null

                                WebResourceResponse(mimeType, "utf-8", inputStream).apply {
                                    responseHeaders = mapOf(
                                        "Access-Control-Allow-Origin" to "*",
                                        "Cache-Control" to "no-cache"
                                    )
                                }
                            } catch (e: Exception) {
                                WebResourceResponse(
                                    "text/plain", "utf-8",
                                    ByteArrayInputStream("Error loading content".toByteArray())
                                )
                            }
                        }

                        else -> assetLoader.shouldInterceptRequest(url)
                    }
                }

            }

            addJavascriptInterface(object {
                @JavascriptInterface
                fun updateStateFromWeb(json: String) {
                    Handler(Looper.getMainLooper()).post {
                        _buttonStatesJson.value = json
                    }
                }
                @JavascriptInterface
                fun updateToolbarVisibility(visibleStr: String) {
                    Handler(Looper.getMainLooper()).post {
                        _shouldShowToolbar.value = visibleStr == "true"
                    }
                }
                @JavascriptInterface
                fun onReceiveTitleAndContent(data: String) {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            val jsonObj = JSONObject(data)
                            _jsonContent.value = jsonObj.optString("json")
                            _plainTextContent.value = jsonObj.optString("plain")
                            _titleString.value = jsonObj.optString("title")
                            Log.d("DBTest", "Before Reset")
                            Log.d("DBTest", "_jsonContent = ${TextEditorEngine.jsonContent.value}\n_plainTextContent = ${TextEditorEngine.plainTextContent.value}\n_titleString = ${TextEditorEngine.titleString.value}")

                        } catch (e: JSONException) {
                            Log.e("TextEditorEngine", "Invalid JSON in onReceiveTitleAndContent", e)
                        }
                    }
                }
                @JavascriptInterface
                fun updateUndoStateFromWeb(json: String) {
                    Handler(Looper.getMainLooper()).post {
                        _undoStatesJson.value = json
                    }
                }
                @JavascriptInterface
                fun onTitleBlur(title: String) {
                    _titleString.value = title
                }
            }, "AndroidBridge")

            // Load index.html từ assets
            loadUrl("https://appassets.androidplatform.net/index.html")
        }
    }

    suspend fun waitForContentUpdate(timeoutMillis: Long = 3000): Triple<String, String, String> =
        suspendCancellableCoroutine { cont ->
            val initialJson = jsonContent.value
            val initialPlain = plainTextContent.value
            val initialTitle = titleString.value

            refreshContentFromWeb()

            val handler = Handler(Looper.getMainLooper())
            val startTime = System.currentTimeMillis()

            val checkRunnable = object : Runnable {
                override fun run() {
                    val newJson = jsonContent.value
                    val newPlain = plainTextContent.value
                    val newTitle = titleString.value

                    if (newJson != initialJson || newPlain != initialPlain || newTitle != initialTitle) {
                        cont.resume(Triple(newTitle, newJson, newPlain))
                    } else if (System.currentTimeMillis() - startTime > timeoutMillis) {
                        cont.resume(Triple(newTitle, newJson, newPlain)) // Trả về dù không thay đổi
                    } else {
                        handler.postDelayed(this, 50)
                    }
                }
            }

            handler.post(checkRunnable)
            cont.invokeOnCancellation {
                handler.removeCallbacks(checkRunnable)
            }
        }

    fun refreshContentFromWeb() {
        if (!::_webView.isInitialized) return

        val js = """
        javascript:
        if (window.getTitleAndContent) {
            window.getTitleAndContent();
        } else {
            console.error("getTitleAndContent is not defined");
        }
    """.trimIndent()

        _webView.post {
            _webView.evaluateJavascript(js, null)
        }
    }

    fun setContent(title: String, content: String) {
        if (!::_webView.isInitialized) return

        val js = """
        javascript:
        if (window.setEditorContent) {
            window.setEditorContent(${JSONObject.quote(title)}, ${JSONObject.quote(content)});
        } else {
            console.error("setEditorContent is not defined");
        }
    """.trimIndent()
        _webView.post { _webView.evaluateJavascript(js, null) }
    }

    fun destroy() {
        if (::_webView.isInitialized) {
            _webView.destroy()
        }
    }
}
