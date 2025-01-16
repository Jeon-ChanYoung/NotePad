package com.chanyoung.notepad

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NoteActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var memoEditText: EditText

    private lateinit var title: String
    private lateinit var content: String
    private lateinit var itemId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        setupUI() // 초기 UI 설정
        initializeViews() // 제목EditText, 메모EditText 찾기
        loadIntentData() // 이전 화면에서 전달받은 데이터 로드
        setInitialData() // 이전 화면에서 전달받은 데이터로 제목EditText, 메모EditText 초기화
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
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
        memoEditText.requestFocus()
        memoEditText.post { memoEditText.scrollTo(0, 0) }
    }

    // 뒤로가기 버튼 동작 설정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                saveNote() // 메모 저장
                finish() // 현재 액티비티 종료
                overridePendingTransition(0, 0) // 화면 전환시 애니매이션 제거
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 소프트키 뒤로가기(홈키 옆에 있는) 버튼 동작 설정
    override fun onBackPressed() {
        saveNote() // 메모 저장
        super.onBackPressed() // 기본 뒤로가기 동작 실행
        overridePendingTransition(0, 0) // 화면 전환시 애니매이션 제거
    }

    private fun saveNote() {
        val title = titleEditText.text.toString()
        val content = memoEditText.text.toString()

        Log.d("test", "Saving Note - Title: $title, Content: $content, itemId: $itemId")

        // 이전 화면으로 데이터 전달
        val intent = intent
        intent.putExtra("title", title)
        intent.putExtra("content", content)
        intent.putExtra("itemId", itemId)
        setResult(RESULT_OK, intent)
    }
}