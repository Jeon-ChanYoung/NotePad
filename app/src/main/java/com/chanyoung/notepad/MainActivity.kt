package com.chanyoung.notepad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class MainActivity : AppCompatActivity() {
    
    private lateinit var themeManager: ThemeManager
    
    private lateinit var memoTitles: ArrayList<String>
    private lateinit var memoContent: ArrayList<String>
    private lateinit var memoItemId: ArrayList<String>
    private lateinit var customAdapter: CustomAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: ImageButton

    private lateinit var adView: AdView
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var constraintSet: ConstraintSet

    private var isAddMemoLocationBotton: Boolean = false

    private val itemTouchHelper by lazy {
        ItemTouchHelper(itemTouchCallback)
    }

    companion object {
        const val SHARED_PREFS_NAME = "NotePadPrefs"
        const val SHARED_PREFS_UI = "NotePadUI"
        const val UNIQUE_ID_KEY = "uniqueId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initializeViews() // 뷰 초기화
        loadMemoData() // 메모 데이터 로드
        setupRecyclerView() // 리스트뷰 설정
        setupAddMemoButton() // + 버튼 설정
        initializeAdMob() // 광고 초기화
        setupUI()  // UI 설정
    }

    // activity_main 초기 UI 설정
    private fun setupUI() {
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 다크모드 x
        themeManager = ThemeManager(this)
        themeManager.applyThemeColors()
        themeManager.changeAddButtonColor(addButton)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // 상태바 위젯 색상 하얀색으로 설정

        updateAddMemoLocation()
    }

    private fun initializeAdMob() {
        adView = findViewById(R.id.adView)
        addButton = findViewById(R.id.addButton)
        constraintLayout = findViewById(R.id.main)
        constraintSet = ConstraintSet()

        // 화면 로딩 후에 광고를 로드
        adView.post {
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            // 광고가 로드된 후 addButton 위치 변경
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.addButton, ConstraintSet.BOTTOM, R.id.adView, ConstraintSet.TOP)
                    constraintSet.applyTo(constraintLayout)
                }
            }
        }
    }

    private fun initializeViews() {
        recyclerView  = findViewById(R.id.recyclerView)
        addButton = findViewById(R.id.addButton)
    }

    private fun loadMemoData() {
        val (titles, contents, itemIds) = loadMemoDataFromPrefs()
        memoTitles = titles
        memoContent = contents
        memoItemId = itemIds
    }


    private fun setupRecyclerView() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val isButtonHidden = sharedPreferences.getBoolean("isButtonHidden", false)

        customAdapter = CustomAdapter(
            this,
            memoTitles,
            !isButtonHidden,
            ::deleteMemo,
            ::openMemoForEditing,
            itemTouchHelper
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter

        // 각 아이템 구분선 추가
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // ItemTouchHelper 연결
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupAddMemoButton() {
        addButton.setOnClickListener {
            // 초기값 설정 밑 저장
            val newTitle = "제목없음"
            val newMemo = ""
            val newId = UUID.randomUUID().toString() // 고유한 ID 생성

            if (isAddMemoLocationBotton) {
                memoTitles.add(newTitle)
                memoContent.add(newMemo)
                memoItemId.add(newId)
                customAdapter.notifyItemInserted(memoTitles.size - 1)
                recyclerView.scrollToPosition(memoTitles.size - 1)
            } else {
                memoTitles.add(0, newTitle)
                memoContent.add(0, newMemo)
                memoItemId.add(0, newId)
                customAdapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)
            }
            saveData(newTitle, newMemo, newId)
        }
    }

    private fun deleteMemo(position: Int) {
        memoTitles.removeAt(position)
        memoContent.removeAt(position)
        memoItemId.removeAt(position)
        saveAllData()
        customAdapter.notifyItemRemoved(position)
    }

    private fun openMemoForEditing(position: Int) {
        val intent = Intent(this, NoteActivity::class.java).apply {
            putExtra("title", memoTitles[position])
            putExtra("content", memoContent[position])
            putExtra("itemId", memoItemId[position])
            putExtra("position", position)
        }
        startActivityForResult(intent, 1)
        overridePendingTransition(0, 0)
    }

    private fun loadMemoDataFromPrefs(): Triple<ArrayList<String>, ArrayList<String>, ArrayList<String>> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val defaultList = ArrayList<String>()

        // 저장된 JSON 문자열 가져오기
        val json = sharedPreferences.getString(UNIQUE_ID_KEY, "{}") ?: "{}"

        return try {
            // JSON 문자열을 MutableMap<String, Pair<String, String>>로 변환
            val typeToken = object : TypeToken<MutableMap<String, Pair<String, String>>>() {}.type
            val currentData: MutableMap<String, Pair<String, String>> =
                Gson().fromJson(json, typeToken) ?: mutableMapOf()

            // title, content, itemId를 각각 추출하여 반환
            val titles = ArrayList(currentData.values.map { it.first })
            val contents = ArrayList(currentData.values.map { it.second })
            val itemIds = ArrayList(currentData.keys)

            Triple(titles, contents, itemIds)
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(defaultList, defaultList, defaultList)
        }
    }

    // 데이터를 SharedPreferences에 저장
    private fun saveData(title: String, memo: String, itemId: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 기존 데이터를 가져오기
        val json = sharedPreferences.getString(UNIQUE_ID_KEY, "[]") ?: "[]"
        val typeToken = object : TypeToken<MutableMap<String, Pair<String, String>>>() {}.type
        val currentData: MutableMap<String, Pair<String, String>> =
            Gson().fromJson(json, typeToken) ?: mutableMapOf()

        // 새로운 데이터를 추가
        currentData[itemId] = Pair(title, memo)

        // JSON으로 변환하여 SharedPreferences에 저장
        val updatedJson = Gson().toJson(currentData)
        editor.putString(UNIQUE_ID_KEY, updatedJson)
        editor.apply()
    }

    private fun saveAllData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val dataMap = memoItemId.mapIndexed { index, itemId ->
            itemId to Pair(memoTitles[index], memoContent[index])
        }.toMap()

        val updatedJson = Gson().toJson(dataMap)
        editor.putString(UNIQUE_ID_KEY, updatedJson)
        editor.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val itemId = data?.getStringExtra("itemId") ?: return
            val index = memoItemId.indexOf(itemId)

            if (index == -1) {
                Log.e("test", "Error: itemId not found in memoItemId. itemId: $itemId")
                return
            }

            if (data.getBooleanExtra("delete", false)) {
                // 메모 삭제 처리
                memoTitles.removeAt(index)
                memoContent.removeAt(index)
                memoItemId.removeAt(index)
                customAdapter.notifyItemRemoved(index)
            } else {
                // 메모 수정 처리
                val title = data.getStringExtra("title") ?: ""
                val content = data.getStringExtra("content") ?: ""

                memoTitles[index] = title
                memoContent[index] = content
                saveData(title, content, itemId)
                customAdapter.notifyItemChanged(index)
            }
        }
    }

    private fun updateAddMemoLocation() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        isAddMemoLocationBotton = sharedPreferences.getBoolean("isAddMemoLocationBotton", false)
    }


    private val itemTouchCallback = object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = source.adapterPosition
            val toPosition = target.adapterPosition

            val itemTitle = memoTitles.removeAt(fromPosition)
            val itemContent = memoContent.removeAt(fromPosition)
            val itemId = memoItemId.removeAt(fromPosition)

            memoTitles.add(toPosition, itemTitle)
            memoContent.add(toPosition, itemContent)
            memoItemId.add(toPosition, itemId)

            saveAllData()

            customAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        // 드래그 중일 땐 아이템 뷰 비활성화
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.titleTextView)?.isEnabled = false
            viewHolder.itemView.findViewById<ImageButton>(R.id.deleteButton)?.isEnabled = false
            viewHolder.itemView.findViewById<ImageButton>(R.id.dragButton)?.isEnabled = false
        }

        // 드래그 시작 시 아이템 뷰 활성화
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if (viewHolder != null) {
                viewHolder.itemView.findViewById<TextView>(R.id.titleTextView)?.isEnabled = true
                viewHolder.itemView.findViewById<ImageButton>(R.id.deleteButton)?.isEnabled = true
                viewHolder.itemView.findViewById<ImageButton>(R.id.dragButton)?.isEnabled = true
            }
        }

        override fun isLongPressDragEnabled(): Boolean = false
        override fun isItemViewSwipeEnabled(): Boolean = false
    }


    // Activity 종료 시 광고 리소스 해제
    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
        themeManager.applyThemeColors()
        themeManager.changeAddButtonColor(addButton)
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_UI, MODE_PRIVATE)
        val isDeleteButtonVisible = sharedPreferences.getBoolean("isButtonHidden", false)
        customAdapter.updateDeleteButtonVisibility(isDeleteButtonVisible)
        updateAddMemoLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
