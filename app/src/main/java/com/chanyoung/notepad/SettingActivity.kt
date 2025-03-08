package com.chanyoung.notepad

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class SettingActivity : AppCompatActivity() {

    companion object {
        const val SHARED_PREFS_UI = "NotePadUI"
    }

    private lateinit var themeManager: ThemeManager
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setupUI()
        initializeAdMob()
        setupRadioButtonChecked()
        setRadioButton()
        setupDeleteHiddenSwitch()
        setupAddMemoLocationSwitch()
    }

    // activity_main 초기 UI 설정
    private fun setupUI() {
        supportActionBar?.apply {
            title = "설정"
            setDisplayHomeAsUpEnabled(true)
        }
        themeManager = ThemeManager(this)
        themeManager.applyThemeColors()
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


    private fun setRadioButton() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedColor = when (checkedId) {
                R.id.btnPurple -> "purple"
                R.id.btnRed -> "red"
                R.id.btnOrange -> "orange"
                R.id.btnGreen -> "green"
                R.id.btnBlue -> "blue"
                R.id.btnGray -> "gray"
                else -> "purple"
            }
            themeManager.saveThemeColor(selectedColor)
            themeManager.applyThemeColors()
            setupDeleteHiddenSwitch()
            setupAddMemoLocationSwitch()
        }
    }

    private fun setupRadioButtonChecked() {
        val selectedColor = themeManager.getThemeColor() // ThemeManager에서 저장된 테마 색상을 가져옵니다.
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        when (selectedColor) {
            "red" -> radioGroup.check(R.id.btnRed)
            "orange" -> radioGroup.check(R.id.btnOrange)
            "purple" -> radioGroup.check(R.id.btnPurple)
            "green" -> radioGroup.check(R.id.btnGreen)
            "blue" -> radioGroup.check(R.id.btnBlue)
            "gray" -> radioGroup.check(R.id.btnGray)
            else -> radioGroup.check(R.id.btnPurple)
        }
    }

    private fun setupDeleteHiddenSwitch() {
        val switchCompat = findViewById<SwitchCompat>(R.id.switch_hide_button)
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeButtonVisibility(true)
            } else {
                changeButtonVisibility(false)
            }
        }

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val isButtonHidden = sharedPreferences.getBoolean("isButtonHidden", false)
        switchCompat.isChecked = isButtonHidden

        val thumbColor = themeManager.getColor() // 테마에서 밝은 색상 가져오기
        val trackColor = themeManager.getBrightColor() // 테마에서 밝은 색상 가져오기 (OFF 상태)

        // 🎨 Thumb (스위치 원 부분) 색상 변경
        val thumbStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // 스위치 ON
                intArrayOf(-android.R.attr.state_checked) // 스위치 OFF
            ),
            intArrayOf(thumbColor, ContextCompat.getColor(this, R.color.brightGray)) // ON일 때 색, OFF일 때 색
        )

        // 🎨 Track (스위치 배경) 색상 변경
        val trackStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(trackColor, ContextCompat.getColor(this, R.color.lightGray))
        )

        // 색상 적용
        switchCompat.thumbTintList = thumbStates
        switchCompat.trackTintList = trackStates
    }

    private fun changeButtonVisibility(hide: Boolean) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isButtonHidden", hide)
        editor.apply()
    }

    private fun setupAddMemoLocationSwitch() {
        val switchCompat = findViewById<SwitchCompat>(R.id.switch_location_button)
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAddMemoLocation(true)
            } else {
                changeAddMemoLocation(false)
            }
        }

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val isAddMemoLocation = sharedPreferences.getBoolean("isAddMemoLocationBotton", false)
        switchCompat.isChecked = isAddMemoLocation

        val thumbColor = themeManager.getColor() // 테마에서 밝은 색상 가져오기
        val trackColor = themeManager.getBrightColor() // 테마에서 밝은 색상 가져오기 (OFF 상태)

        // 🎨 Thumb (스위치 원 부분) 색상 변경
        val thumbStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // 스위치 ON
                intArrayOf(-android.R.attr.state_checked) // 스위치 OFF
            ),
            intArrayOf(thumbColor, ContextCompat.getColor(this, R.color.brightGray)) // ON일 때 색, OFF일 때 색
        )

        // 🎨 Track (스위치 배경) 색상 변경
        val trackStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(trackColor, ContextCompat.getColor(this, R.color.lightGray))
        )

        // 색상 적용
        switchCompat.thumbTintList = thumbStates
        switchCompat.trackTintList = trackStates
    }

    private fun changeAddMemoLocation(hide: Boolean) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isAddMemoLocationBotton", hide)
        editor.apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(0, 0)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}