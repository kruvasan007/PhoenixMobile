package com.example.phoenixmobile.ui.chat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.phoenixmobile.R

class TypingIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var avatarSystem: ImageView
    private lateinit var tvTyping: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View

    private var animatorSet: AnimatorSet? = null

    init {
        setupView()
    }

    private fun setupView() {
        orientation = HORIZONTAL
        setPadding(16, 8, 16, 8)

        // Создаем аватар системы
        avatarSystem = ImageView(context).apply {
            layoutParams = LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                marginEnd = 12.dpToPx()
            }
            setImageResource(R.drawable.ic_system_24)
            background = context.getDrawable(R.drawable.system_avatar_bg)
            setPadding(8, 8, 8, 8)
        }
        addView(avatarSystem)

        // Создаем контейнер для текста и точек
        val typingContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        // Текст "Система печатает..."
        tvTyping = TextView(context).apply {
            text = "Система печатает"
            textSize = 12f
            setTextColor(context.getColor(android.R.color.darker_gray))
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 4.dpToPx()
            }
        }
        typingContainer.addView(tvTyping)

        // Контейнер для точек
        val dotsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        // Создаем три точки
        dot1 = createDot()
        dot2 = createDot()
        dot3 = createDot()

        dotsContainer.addView(dot1)
        dotsContainer.addView(dot2)
        dotsContainer.addView(dot3)

        typingContainer.addView(dotsContainer)
        addView(typingContainer)

        visibility = GONE
    }

    private fun createDot(): View {
        return View(context).apply {
            layoutParams = LayoutParams(8.dpToPx(), 8.dpToPx()).apply {
                marginEnd = 4.dpToPx()
            }
            background = context.getDrawable(R.drawable.typing_dot)
        }
    }

    fun startTypingAnimation() {
        visibility = VISIBLE

        animatorSet?.cancel()

        // Создаем анимацию для каждой точки с задержкой
        val animator1 = createDotAnimator(dot1, 0L)
        val animator2 = createDotAnimator(dot2, 200L)
        val animator3 = createDotAnimator(dot3, 400L)

        animatorSet = AnimatorSet().apply {
            playTogether(animator1, animator2, animator3)
            start()
        }
    }

    fun stopTypingAnimation() {
        animatorSet?.cancel()
        visibility = GONE

        // Возвращаем точки в исходное состояние
        dot1.alpha = 0.3f
        dot2.alpha = 0.3f
        dot3.alpha = 0.3f
    }

    private fun createDotAnimator(dot: View, startDelay: Long): ObjectAnimator {
        return ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1.0f, 0.3f).apply {
            duration = 600L
            this.startDelay = startDelay
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
