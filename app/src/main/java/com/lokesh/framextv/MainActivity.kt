package com.lokesh.framextv

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.content.SharedPreferences

/** FrameXTV Android TV shell based only on URLs and embed formats shown in the supplied documentation. */
class MainActivity : Activity() {
    private val bg = Color.rgb(13, 15, 21)
    private val panel = Color.rgb(28, 31, 41)
    private val accent = Color.rgb(117, 190, 255)
    private val site = "https://framextv.xyz"
    private lateinit var content: WebView
    private lateinit var pageTitle: TextView
    private lateinit var prefs: SharedPreferences
    private var currentPage = "$site/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("framextv_history", MODE_PRIVATE)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
        openPage("Home", "$site/")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(28, 18, 28, 18)
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "frameXTV"; textSize = 30f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(makeButton("Play by ID") { showPlayDialog() })
        header.addView(makeButton("My History") { showHistory() })
        root.addView(header)

        val navScroll = HorizontalScrollViewCompat(this)
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 12, 0, 12) }
        nav.addView(makeButton("Home") { openPage("Home", "$site/") })
        nav.addView(makeButton("Movies") { openPage("Movies", "$site/movies") })
        nav.addView(makeButton("TV Shows") { openPage("TV Shows", "$site/tv") })
        nav.addView(makeButton("Anime") { openPage("Anime", "$site/anime") })
        nav.addView(makeButton("Search") { openPage("Search", "$site/"); Toast.makeText(this, "Use the site's Search navigation", Toast.LENGTH_LONG).show() })
        nav.addView(makeButton("My List") { openPage("My List", "$site/my-list") })
        nav.addView(makeButton("History") { showHistory() })
        navScroll.addView(nav)
        root.addView(navScroll)

        pageTitle = TextView(this).apply {
            text = "Home"; textSize = 18f; setTextColor(accent); setPadding(2, 2, 2, 10)
        }
        root.addView(pageTitle)
        content = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowContentAccess = true
            settings.loadsImagesAutomatically = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Keep normal FrameXTV navigation inside the app. External links are opened by the WebView as well.
                    return false
                }
            }
            isFocusable = true
            isFocusableInTouchMode = true
        }
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(TextView(this).apply {
            text = "Use the TV remote • Playback uses FrameXTV's documented embed player"
            textSize = 12f; setTextColor(Color.LTGRAY); setPadding(2, 10, 2, 0)
        })
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        isFocusable = true
        isFocusableInTouchMode = true
        textSize = 14f
        setOnClickListener { action() }
        val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        p.marginEnd = 8
        layoutParams = p
    }

    private fun openPage(title: String, url: String) {
        currentPage = url
        pageTitle.text = title
        content.loadUrl(url)
    }

    private fun showPlayDialog() {
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 8, 28, 0) }
        fun field(hint: String, number: Boolean = false): EditText {
            return EditText(this).apply {
                this.hint = hint
                textSize = 18f
                setSingleLine(true)
                if (number) inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setTextColor(Color.WHITE)
            }.also { form.addView(it) }
        }
        val id = field("TMDB ID (required)", true)
        val season = field("Season (TV only)", true)
        val episode = field("Episode (TV only)", true)
        AlertDialog.Builder(this)
            .setTitle("Play on FrameXTV")
            .setMessage("Enter the TMDB ID. For a TV episode, also enter season and episode numbers.")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Play") { _, _ ->
                val tmdb = id.text.toString().trim()
                val s = season.text.toString().trim()
                val e = episode.text.toString().trim()
                if (tmdb.isBlank() || !tmdb.all { it.isDigit() }) {
                    Toast.makeText(this, "Enter a numeric TMDB ID", Toast.LENGTH_LONG).show()
                } else if ((s.isBlank()) != (e.isBlank()) || (s.isNotBlank() && (!s.all { it.isDigit() } || !e.all { it.isDigit() }))) {
                    Toast.makeText(this, "Enter both season and episode, or leave both empty", Toast.LENGTH_LONG).show()
                } else {
                    val url = if (s.isNotBlank()) "$site/embed/$tmdb/$s/$e?autoplay=1&muted=0" else "$site/embed/$tmdb?autoplay=1&muted=0"
                    addHistory(tmdb, s, e)
                    openPage("Now Playing", url)
                }
            }.show()
    }

    private fun addHistory(id: String, season: String, episode: String) {
        val entry = if (season.isNotBlank()) "$id|$season|$episode" else "$id||"
        val old = prefs.getString("items", "").orEmpty().split("\n").filter { it.isNotBlank() && !it.startsWith("$id|") }
        prefs.edit().putString("items", (listOf(entry) + old).take(50).joinToString("\n")).apply()
    }

    private fun showHistory() {
        val items = prefs.getString("items", "").orEmpty().split("\n").filter { it.isNotBlank() }
        if (items.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Watch History").setMessage("Nothing played from Play by ID yet. History is stored locally on this TV.")
                .setPositiveButton("OK", null).show()
            return
        }
        val labels = items.map { raw ->
            val p = raw.split("|")
            if (p.size >= 3 && p[1].isNotBlank()) "TMDB ${p[0]} — S${p[1]} E${p[2]}" else "TMDB ${p[0]}"
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Watch History · Select to replay")
            .setItems(labels) { _, which ->
                val p = items[which].split("|")
                val url = if (p.size >= 3 && p[1].isNotBlank()) "$site/embed/${p[0]}/${p[1]}/${p[2]}?autoplay=1&muted=0" else "$site/embed/${p[0]}?autoplay=1&muted=0"
                openPage("Now Playing", url)
            }
            .setNeutralButton("Clear") { _, _ -> prefs.edit().remove("items").apply(); Toast.makeText(this, "Local history cleared", Toast.LENGTH_SHORT).show() }
            .setNegativeButton("Close", null).show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && content.canGoBack()) {
            content.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (::content.isInitialized) content.destroy()
        super.onDestroy()
    }
}

/** Minimal horizontal container to avoid adding a third-party UI dependency. */
private class HorizontalScrollViewCompat(context: android.content.Context) : android.widget.HorizontalScrollView(context) {
    init { isHorizontalScrollBarEnabled = false; isFillViewport = true }
}
