package com.example.phoenixmobile.ui.chat.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.databinding.ItemMessageBinding
import com.example.phoenixmobile.ui.chat.ChatMessage
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImprovedMessageAdapter : ListAdapter<ChatMessage, ImprovedMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private lateinit var markwon: Markwon

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // Инициализируем Markwon для поддержки базового markdown
        markwon = Markwon.create(recyclerView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding, markwon)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val markwon: Markwon
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            val timeText = timeFormat.format(Date(message.timestamp))

            if (message.isUser) {
                // Показать сообщение пользователя справа
                binding.layoutUserMessage.visibility = View.VISIBLE
                binding.layoutSystemMessage.visibility = View.GONE

                // Применяем markdown форматирование к тексту пользователя
                markwon.setMarkdown(binding.tvUserMessage, message.text)
                binding.tvUserTime.text = timeText

                // Анимация появления справа
                animateFromRight(binding.layoutUserMessage)
            } else {
                // Показать сообщение системы слева
                binding.layoutUserMessage.visibility = View.GONE
                binding.layoutSystemMessage.visibility = View.VISIBLE

                // Применяем markdown форматирование к тексту системы
                markwon.setMarkdown(binding.tvSystemMessage, message.text)
                binding.tvSystemTime.text = timeText

                // Анимация появления слева
                animateFromLeft(binding.layoutSystemMessage)
            }
        }

        private fun animateFromRight(view: View) {
            view.translationX = 300f
            view.alpha = 0f

            ObjectAnimator.ofFloat(view, "translationX", 300f, 0f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }
        }

        private fun animateFromLeft(view: View) {
            view.translationX = -300f
            view.alpha = 0f

            ObjectAnimator.ofFloat(view, "translationX", -300f, 0f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
