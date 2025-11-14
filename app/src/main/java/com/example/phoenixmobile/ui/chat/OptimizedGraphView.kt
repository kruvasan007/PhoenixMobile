package com.example.phoenixmobile.ui.chat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.*
import kotlin.random.Random

/**
 * Граф знаний с улучшенным алгоритмом размещения узлов Фрухтермана-Рейнгольда
 * Предотвращает пересечения линий и создает читаемую структуру
 */
class OptimizedGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Современная цветовая схема
    private val subjectColor = Color.parseColor("#4CAF50") // Material Green
    private val objectColor = Color.parseColor("#2196F3")  // Material Blue
    private val bothColor = Color.parseColor("#FF9800")    // Material Orange
    private val edgeColor = Color.parseColor("#757575")    // Material Grey
    private val textColor = Color.WHITE
    private val backgroundColor = Color.parseColor("#FAFAFA") // Light background

    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = edgeColor
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 20f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val relationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424242") // Dark Grey
        textSize = 14f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val relationBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF") // White background
        style = Paint.Style.FILL
    }

    // Данные графа
    private var triplets: List<Triplet> = emptyList()
    private val graphNodes = mutableMapOf<String, GraphNode>()
    private val graphEdges = mutableListOf<GraphEdge>()

    // Параметры трансформации
    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    // Детекторы жестов
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, PanListener())

    // Параметры алгоритма Фрухтермана-Рейнгольда
    private val area: Float get() = (width * height).toFloat()
    private val k: Float get() = sqrt(area / maxOf(graphNodes.size, 1)).coerceAtLeast(50f)
    private val maxIterations = 50
    private val temperature: Float get() = sqrt(area) / 10f
    private val nodeRadius = 50f

    data class GraphNode(
        var x: Float,
        var y: Float,
        var dx: Float = 0f,
        var dy: Float = 0f,
        val label: String,
        val type: NodeType,
        var degree: Int = 0 // количество связей
    )

    data class GraphEdge(
        val source: String,
        val target: String,
        val label: String
    )

    enum class NodeType { SUBJECT, OBJECT, BOTH }

    init {
        setBackgroundColor(backgroundColor)
        Log.d("OPTIMIZED_GRAPH", "OptimizedGraphView initialized")
    }

    fun setTriplets(newTriplets: List<Triplet>) {
        Log.d("OPTIMIZED_GRAPH", "Setting ${newTriplets.size} triplets")
        triplets = newTriplets
        buildGraph()
        applyForceDirectedLayout()
        invalidate()
    }

    private fun buildGraph() {
        graphNodes.clear()
        graphEdges.clear()

        if (triplets.isEmpty()) return

        // Собираем все узлы
        val subjects = triplets.map { it.subject }.toSet()
        val objects = triplets.map { it.`object` }.toSet()
        val allNodes = subjects + objects

        // Подсчитываем степень каждого узла
        val degrees = mutableMapOf<String, Int>()
        triplets.forEach { triplet ->
            degrees[triplet.subject] = (degrees[triplet.subject] ?: 0) + 1
            degrees[triplet.`object`] = (degrees[triplet.`object`] ?: 0) + 1
        }

        // Создаем узлы с начальным случайным размещением
        allNodes.forEach { nodeName ->
            val nodeType = when {
                subjects.contains(nodeName) && objects.contains(nodeName) -> NodeType.BOTH
                subjects.contains(nodeName) -> NodeType.SUBJECT
                else -> NodeType.OBJECT
            }

            graphNodes[nodeName] = GraphNode(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * height,
                label = nodeName,
                type = nodeType,
                degree = degrees[nodeName] ?: 0
            )
        }

        // Создаем рёбра
        triplets.forEach { triplet ->
            graphEdges.add(GraphEdge(triplet.subject, triplet.`object`, triplet.relation))
        }

        Log.d("OPTIMIZED_GRAPH", "Built graph with ${graphNodes.size} nodes and ${graphEdges.size} edges")
    }

    private fun applyForceDirectedLayout() {
        if (graphNodes.isEmpty()) return

        val temp = temperature

        for (iteration in 0 until maxIterations) {
            val currentTemp = temp * (1 - iteration.toFloat() / maxIterations)

            // Сброс сил
            graphNodes.values.forEach { node ->
                node.dx = 0f
                node.dy = 0f
            }

            // Силы отталкивания между всеми парами узлов
            graphNodes.values.forEach { nodeA ->
                graphNodes.values.forEach { nodeB ->
                    if (nodeA != nodeB) {
                        val dx = nodeA.x - nodeB.x
                        val dy = nodeA.y - nodeB.y
                        val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                        // Сила отталкивания
                        val repulsiveForce = k * k / distance
                        val fx = (dx / distance) * repulsiveForce
                        val fy = (dy / distance) * repulsiveForce

                        nodeA.dx += fx
                        nodeA.dy += fy
                    }
                }
            }

            // Силы притяжения между соединенными узлами
            graphEdges.forEach { edge ->
                val nodeA = graphNodes[edge.source]
                val nodeB = graphNodes[edge.target]

                if (nodeA != null && nodeB != null) {
                    val dx = nodeA.x - nodeB.x
                    val dy = nodeA.y - nodeB.y
                    val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                    // Сила притяжения
                    val attractiveForce = distance * distance / k
                    val fx = (dx / distance) * attractiveForce
                    val fy = (dy / distance) * attractiveForce

                    nodeA.dx -= fx
                    nodeA.dy -= fy
                    nodeB.dx += fx
                    nodeB.dy += fy
                }
            }

            // Применяем силы с учетом температуры
            graphNodes.values.forEach { node ->
                val displacement = sqrt(node.dx * node.dx + node.dy * node.dy).coerceAtLeast(1f)
                val limitedDisplacement = minOf(displacement, currentTemp)

                node.x += (node.dx / displacement) * limitedDisplacement
                node.y += (node.dy / displacement) * limitedDisplacement

                // Ограничиваем узлы границами экрана с отступом
                val margin = nodeRadius + 20f
                node.x = node.x.coerceIn(margin, (width - margin).coerceAtLeast(margin))
                node.y = node.y.coerceIn(margin, (height - margin).coerceAtLeast(margin))
            }
        }

        Log.d("OPTIMIZED_GRAPH", "Force-directed layout completed after $maxIterations iterations")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Пересчитываем размещение при изменении размера
        if (graphNodes.isNotEmpty()) {
            applyForceDirectedLayout()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (triplets.isEmpty() || graphNodes.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        // Рисуем рёбра
        drawEdges(canvas)

        // Рисуем узлы поверх рёбер
        drawNodes(canvas)

        canvas.restore()
    }

    private fun drawEmptyState(canvas: Canvas) {
        val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CD853F")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val lines = arrayOf(
            "Graph is empty"
        )

        val lineHeight = 30f
        val startY = height / 2f - (lines.size * lineHeight) / 2f

        lines.forEachIndexed { index, line ->
            canvas.drawText(line, width / 2f, startY + index * lineHeight, emptyPaint)
        }
    }

    private fun drawEdges(canvas: Canvas) {
        graphEdges.forEach { edge ->
            val sourceNode = graphNodes[edge.source]
            val targetNode = graphNodes[edge.target]

            if (sourceNode != null && targetNode != null) {
                drawSmoothEdge(canvas, sourceNode, targetNode, edge.label)
            }
        }
    }

    private fun drawSmoothEdge(canvas: Canvas, source: GraphNode, target: GraphNode, label: String) {
        val dx = target.x - source.x
        val dy = target.y - source.y
        val distance = sqrt(dx * dx + dy * dy)

        // Вычисляем точки на границе узлов
        val unitX = dx / distance
        val unitY = dy / distance

        val startX = source.x + unitX * nodeRadius
        val startY = source.y + unitY * nodeRadius
        val endX = target.x - unitX * nodeRadius
        val endY = target.y - unitY * nodeRadius

        // Создаем плавную кривую для предотвращения пересечений
        val midX = (startX + endX) / 2
        val midY = (startY + endY) / 2

        // Добавляем изгиб перпендикулярно линии
        val perpX = -unitY * 25f
        val perpY = unitX * 25f

        val controlX = midX + perpX
        val controlY = midY + perpY

        // Создаем градиент для рёбра
        val gradient = LinearGradient(
            startX, startY, endX, endY,
            Color.parseColor("#90CAF9"), // Light Blue
            Color.parseColor("#42A5F5"), // Blue
            Shader.TileMode.CLAMP
        )

        val gradientPaint = Paint(edgePaint).apply {
            shader = gradient
            strokeWidth = 4f
            pathEffect = null
        }

        // Рисуем тень рёбра
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#30000000")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }

        val shadowPath = Path()
        shadowPath.moveTo(startX + 2f, startY + 2f)
        shadowPath.quadTo(controlX + 2f, controlY + 2f, endX + 2f, endY + 2f)
        canvas.drawPath(shadowPath, shadowPaint)

        // Рисуем основную кривую с градиентом
        val path = Path()
        path.moveTo(startX, startY)
        path.quadTo(controlX, controlY, endX, endY)
        canvas.drawPath(path, gradientPaint)

        // Рисуем стрелку на конце
        drawArrowHead(canvas, endX, endY, controlX - endX, controlY - endY)

        // Рисуем подпись связи
        drawEdgeLabel(canvas, controlX, controlY, label)
    }

    private fun drawArrowHead(canvas: Canvas, tipX: Float, tipY: Float, dirX: Float, dirY: Float) {
        val length = sqrt(dirX * dirX + dirY * dirY).coerceAtLeast(1f)
        val unitX = dirX / length
        val unitY = dirY / length

        val arrowLength = 20f
        val arrowAngle = PI / 6

        val arrowX1 = tipX + arrowLength * cos(atan2(unitY.toDouble(), unitX.toDouble()) + arrowAngle).toFloat()
        val arrowY1 = tipY + arrowLength * sin(atan2(unitY.toDouble(), unitX.toDouble()) + arrowAngle).toFloat()
        val arrowX2 = tipX + arrowLength * cos(atan2(unitY.toDouble(), unitX.toDouble()) - arrowAngle).toFloat()
        val arrowY2 = tipY + arrowLength * sin(atan2(unitY.toDouble(), unitX.toDouble()) - arrowAngle).toFloat()

        canvas.drawLine(tipX, tipY, arrowX1, arrowY1, edgePaint)
        canvas.drawLine(tipX, tipY, arrowX2, arrowY2, edgePaint)
    }

    private fun drawEdgeLabel(canvas: Canvas, x: Float, y: Float, label: String) {
        val bounds = Rect()
        relationPaint.getTextBounds(label, 0, label.length, bounds)
        val padding = 6f

        // Фон для подписи
        canvas.drawRoundRect(
            x - bounds.width() / 2f - padding,
            y - bounds.height() / 2f - padding,
            x + bounds.width() / 2f + padding,
            y + bounds.height() / 2f + padding,
            6f, 6f, relationBgPaint
        )

        // Текст подписи
        canvas.drawText(label, x, y + bounds.height() / 2f, relationPaint)
    }

    private fun drawNodes(canvas: Canvas) {
        graphNodes.values.forEach { node ->
            drawNode(canvas, node)
        }
    }

    private fun drawNode(canvas: Canvas, node: GraphNode) {
        // Выбираем цвет в зависимости от типа узла
        val color = when (node.type) {
            NodeType.SUBJECT -> subjectColor
            NodeType.OBJECT -> objectColor
            NodeType.BOTH -> bothColor
        }

        // Размер узла зависит от количества связей
        val radius = nodeRadius + (node.degree * 3f).coerceAtMost(15f)

        // Рисуем тень
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint.color = 0x40000000
        shadowPaint.style = Paint.Style.FILL
        canvas.drawCircle(node.x + 4f, node.y + 4f, radius, shadowPaint)

        // Создаем радиальный градиент для узла
        val gradient = RadialGradient(
            node.x - radius * 0.3f, node.y - radius * 0.3f, radius,
            intArrayOf(
                Color.WHITE,
                color,
                darkenColor(color)
            ),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        nodePaint.shader = gradient
        nodePaint.color = color
        nodePaint.style = Paint.Style.FILL

        // Рисуем узел с градиентом
        canvas.drawCircle(node.x, node.y, radius, nodePaint)

        // Сбрасываем градиент
        nodePaint.shader = null

        // Рисуем тонкую обводку
        nodePaint.color = darkenColor(color)
        nodePaint.style = Paint.Style.STROKE
        nodePaint.strokeWidth = 2f
        canvas.drawCircle(node.x, node.y, radius, nodePaint)

        // Рисуем текст с тенью
        val textShadowPaint = Paint(textPaint)
        textShadowPaint.color = 0x80000000.toInt()

        val displayText = if (node.label.length > 12) {
            node.label.substring(0, 12) + "…"
        } else {
            node.label
        }

        // Рисуем тень текста
        canvas.drawText(displayText, node.x + 1f, node.y + 7f, textShadowPaint)
        // Рисуем основной текст
        canvas.drawText(displayText, node.x, node.y + 6f, textPaint)
    }

    private fun darkenColor(color: Int): Int {
        val factor = 0.8f
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.rgb(r, g, b)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
            invalidate()
            return true
        }
    }

    private inner class PanListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            translateX -= distanceX
            translateY -= distanceY
            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Двойной тап сбрасывает масштаб
            scaleFactor = 1f
            translateX = 0f
            translateY = 0f
            invalidate()
            return true
        }
    }
}
