package com.example.phoenixmobile.ui.chat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import com.example.phoenixmobile.ui.chat.ChatMessage

class ChatAdapter(private var data: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatVH>() {
    fun submit(list: List<ChatMessage>) {
        data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatVH(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ChatVH, position: Int) = holder.bind(data[position])

    class ChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.tvSystemMessage)
        private val card: CardView = itemView.findViewById(R.id.card)

        fun bind(msg: ChatMessage) {
            if (msg.isUser) {
                text.text = "👤 Вы: ${msg.text}"
                card.setCardBackgroundColor(Color.parseColor("#FFE4B5")) // Moccasin - для пользователя
                text.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            } else {
                text.text = "🤖 Ассистент: ${msg.text}"
                card.setCardBackgroundColor(Color.parseColor("#FFF8DC")) // Cornsilk - для системы
                text.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            }
        }
    }
}