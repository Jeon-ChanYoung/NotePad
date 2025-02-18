package com.chanyoung.notepad

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var memoEditText: EditText
    private lateinit var titleEditTextOriginal: String
    private lateinit var memoEditTextOriginal: String

    private lateinit var title: String
    private lateinit var content: String
    private lateinit var itemId: String

    private lateinit var adView: AdView

    private var isSavedMemo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        setupUI() // 초기 UI 설정
        initializeAdMob() // 하단 광고 배너 설정
        initializeViews() // 제목EditText, 메모EditText 찾기
        loadIntentData() // 이전 화면에서 전달받은 데이터 로드
        setInitialData() // 이전 화면에서 전달받은 데이터로 제목EditText, 메모EditText 초기화
    }

    private fun setupUI() {
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.purple)) // 상태바 색상 설정
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.brightPurple)) // 네비게이션 바 색상 설정

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, // 밝은 배경에 어두운 텍스트
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun initializeAdMob() {
        adView = findViewById(R.id.adView)

        // 화면 로딩 후에 광고를 로드
        adView.post {
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        memoEditText = findViewById(R.id.memoEditText)
    }

    private fun loadIntentData() {
        title = intent.getStringExtra("title") ?: ""
        content = intent.getStringExtra("content") ?: ""
        itemId = intent.getStringExtra("itemId") ?: ""
    }

    private fun setInitialData() {
        titleEditText.setText(title)
        memoEditText.setText(content)
        titleEditTextOriginal = title
        memoEditTextOriginal = content

        memoEditText.requestFocus()
        memoEditText.post { memoEditText.scrollTo(0, 0) }
    }

    // 뒤로가기 버튼 동작 설정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                saveNoteToPrefs() // 메모 저장
                finish() // 현재 액티비티 종료
                overridePendingTransition(0, 0) // 화면 전환시 애니매이션 제거
                true
            }

            R.id.copy -> {
                copyMemoToClipboard()
                true
            }

            R.id.share -> {
                shareNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 소프트키 뒤로가기(홈키 옆에 있는) 버튼 동작 설정
    override fun onBackPressed() {
        saveNoteToPrefs() // 메모 저장
        super.onBackPressed() // 기본 뒤로가기 동작 실행
        overridePendingTransition(0, 0) // 화면 전환시 애니매이션 제거
    }

    // 홈키 버튼 동작 설정
    override fun onPause() {
        saveNoteToPrefs() // 메모 저장
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    private fun saveNoteToPrefs() {
        if (isSavedMemo) {
            return
        }
        isSavedMemo = true

        val title = titleEditText.text.toString()
        val content = memoEditText.text.toString()

        Log.d("test", "Saving Note - Title: $title, Content: $content, itemId: $itemId")

        // 변경사항 없으면 패스
        if (title == titleEditTextOriginal && content == memoEditTextOriginal) {
            setResult(RESULT_CANCELED)
            return
        }

        // Toast 메시지 띄우기
        Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()

        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 기존 데이터 불러오기
        val json = sharedPreferences.getString(MainActivity.UNIQUE_ID_KEY, "{}") ?: "{}"
        val typeToken = object : TypeToken<MutableMap<String, Pair<String, String>>>() {}.type
        val currentData: MutableMap<String, Pair<String, String>> =
            Gson().fromJson(json, typeToken) ?: mutableMapOf()

        // 현재 메모 데이터를 저장
        currentData[itemId] = Pair(titleEditText.text.toString(), memoEditText.text.toString())

        // JSON 변환 후 저장
        val updatedJson = Gson().toJson(currentData)
        editor.putString("uniqueId", updatedJson)
        editor.apply()

        // 결과 설정
        intent.apply {
            putExtra("title", title)
            putExtra("content", content)
            putExtra("itemId", itemId)
            setResult(RESULT_OK, this)
        }
    }

    private fun copyMemoToClipboard() {
        val content = memoEditText.text.toString()

        if (content.isEmpty()) {
            Toast.makeText(this, "복사할 메모가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("메모", content)
        clipboard.setPrimaryClip(clip)
    }

    private fun shareNote() {
        val content = memoEditText.text.toString()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }
        startActivity(Intent.createChooser(shareIntent, "메모 공유"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_activity_menu_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}