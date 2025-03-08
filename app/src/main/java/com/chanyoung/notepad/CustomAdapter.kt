package com.chanyoung.notepad

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    private val context: Context,
    private val titles: ArrayList<String>,
    private var isDeleteButtonHidden: Boolean,
    private val onDelete: (position: Int) -> Unit,
    private val onClick: (position: Int) -> Unit,
    private val itemTouchHelper: ItemTouchHelper
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    // ViewHolder 클래스 정의
    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val dragButton: ImageButton = itemView.findViewById(R.id.dragButton)

        init {
            // 메모 항목 클릭 시
            itemView.setOnClickListener { onClick(adapterPosition) }

            // 삭제 버튼 클릭 시
            deleteButton.setOnClickListener { showDeleteDialog(adapterPosition) }

            // 드래그 버튼 클릭 시 드래그 시작
            dragButton.setOnTouchListener { _, _ ->
                itemTouchHelper.startDrag(this) // ViewHolder를 직접적으로 전달
                false
            }
        }

        // 삭제 다이얼로그 표시
        private fun showDeleteDialog(position: Int) {
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("삭제")
                .setMessage("이 메모를 삭제하겠습니까?")
                .setPositiveButton("네") { _, _ -> onDelete(position) }
                .setNegativeButton("아니요", null)
                .create()

            alertDialog.show()
            // 다이얼로그 버튼 색상 변경
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = titles[position]
        holder.titleTextView.text = title

        // 삭제 버튼의 가시성 설정
        holder.deleteButton.visibility = if (isDeleteButtonHidden) View.GONE else View.VISIBLE

        // 각 항목에 대한 고유한 contentDescription 설정
        holder.titleTextView.contentDescription = "메모 항목 ${position + 1}"
        holder.deleteButton.contentDescription = "삭제 버튼 ${position + 1}"
        holder.dragButton.contentDescription = "드래그 버튼 ${position + 1}"
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    fun updateDeleteButtonVisibility(isVisible: Boolean) {
        isDeleteButtonHidden = isVisible
        notifyDataSetChanged()
    }
}