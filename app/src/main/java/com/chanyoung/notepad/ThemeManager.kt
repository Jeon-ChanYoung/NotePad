package com.chanyoung.notepad

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ThemeManager(private val context: Context) {

    companion object {
        const val SHARED_PREFS_UI = "NotePadUI"
    }

    // 색상 테마를 SharedPreferences에 저장
    fun saveThemeColor(color: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_UI, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("themeColor", color)
        editor.apply()
    }

    fun getThemeColor(): String {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_UI, Context.MODE_PRIVATE)
        return sharedPreferences.getString("themeColor", "purple") ?: "purple"
    }

    fun getColor(): Int {
        val color = getThemeColor()
        return when (color) {
            "red" -> ContextCompat.getColor(context, R.color.red)
            "orange" -> ContextCompat.getColor(context, R.color.orange)
            "green" -> ContextCompat.getColor(context, R.color.green)
            "blue" -> ContextCompat.getColor(context, R.color.blue)
            "gray" -> ContextCompat.getColor(context, R.color.gray)
            else -> ContextCompat.getColor(context, R.color.purple)
        }
    }

    fun getLightColor(): Int {
        val color = getThemeColor()
        return when (color) {
            "red" ->  ContextCompat.getColor(context, R.color.lightRed)
            "orange" -> ContextCompat.getColor(context, R.color.lightOrange)
            "green" -> ContextCompat.getColor(context, R.color.lightGreen)
            "blue" -> ContextCompat.getColor(context, R.color.lightBlue)
            "gray" -> ContextCompat.getColor(context, R.color.lightGray)
            else -> ContextCompat.getColor(context, R.color.lightPurple)
        }
    }

    fun getBrightColor(): Int {
        val color = getThemeColor()
        return when (color) {
            "red" ->  ContextCompat.getColor(context, R.color.brightRed)
            "orange" -> ContextCompat.getColor(context, R.color.brightOrange)
            "green" -> ContextCompat.getColor(context, R.color.brightGreen)
            "blue" -> ContextCompat.getColor(context, R.color.brightBlue)
            "gray" -> ContextCompat.getColor(context, R.color.brightGray)
            else -> ContextCompat.getColor(context, R.color.brightPurple)
        }
    }

    // 저장된 테마 색상에 맞게 상태바와 네비게이션 바, 타이틀 바 색상 설정
    fun applyThemeColors() {
        val lightColor = getLightColor()
        val statusBarColor = getColor()
        val navigationBarColor = getBrightColor()

        // 상태바 색상 설정
        val window = (context as AppCompatActivity).window
        window.statusBarColor = statusBarColor

        // 타이틀(상단 바, 제목) 바 색상 설정
        context.supportActionBar?.setBackgroundDrawable(ColorDrawable(lightColor))

        // 네비게이션 바 색상 설정
        window.setNavigationBarColor(navigationBarColor)

        // 안드로이드 11 이상에서 네비게이션 바 색상 및 텍스트 스타일 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }

    // + 버튼 색깔 변경
    fun changeAddButtonColor(addButton: ImageButton) {
        val buttonColor = getLightColor()
        val drawable = ContextCompat.getDrawable(context, R.drawable.rounded_button)!!
        val shapeDrawable = drawable.mutate()
        shapeDrawable.setTint(buttonColor)
        addButton.background = shapeDrawable
    }

    // EditText 밑줄 색상 및 드래그 영역 색깔 변경
    @SuppressLint("NewApi")
    fun changeFocusColor(titleEditText: EditText, memoEditText: EditText) {
        val focusColor = getLightColor()
        val scrollColor = getBrightColor()

        titleEditText.backgroundTintList = ColorStateList.valueOf(focusColor)
        titleEditText.highlightColor = focusColor
        titleEditText.textSelectHandle?.setTint(focusColor)
        titleEditText.textSelectHandleLeft?.setTint(focusColor)
        titleEditText.textSelectHandleRight?.setTint(focusColor)
        val titleCursorDrawable = GradientDrawable().apply {
            setColor(focusColor)
            setSize(6, titleEditText.lineHeight)
        }
        titleEditText.textCursorDrawable = titleCursorDrawable

        memoEditText.highlightColor = focusColor
        memoEditText.textSelectHandle?.setTint(focusColor)
        memoEditText.textSelectHandleLeft?.setTint(focusColor)
        memoEditText.textSelectHandleRight?.setTint(focusColor)
        val memoCursorDrawable = GradientDrawable().apply {
            setColor(focusColor)
            setSize(6, titleEditText.lineHeight)
        }
        memoEditText.textCursorDrawable = memoCursorDrawable

        val drawable = ContextCompat.getDrawable(context, R.drawable.custom_scrollbar)!!
        val shapeDrawable = drawable.mutate()
        shapeDrawable.setTint(scrollColor)
        memoEditText.verticalScrollbarThumbDrawable = shapeDrawable
    }
}
