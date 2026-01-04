package es.etg.lectoguard.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import es.etg.lectoguard.R
import es.etg.lectoguard.domain.model.ReadingStatus

class ReadingStatsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stats: Map<ReadingStatus, Int> = emptyMap()
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 36f
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.colorOnBackground)
    }
    
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.colorOnSurfaceVariant)
    }

    fun updateStats(newStats: Map<ReadingStatus, Int>) {
        stats = newStats
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val total = stats.values.sum()
        if (total == 0) {
            // Mostrar mensaje cuando no hay datos
            canvas.drawText("No hay datos", width / 2f, height / 2f, textPaint)
            return
        }
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) * 0.35f).coerceAtMost(120f)
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        var startAngle = -90f // Comenzar desde arriba
        
        // Colores para cada estado
        val colors = mapOf(
            ReadingStatus.WANT_TO_READ to context.getColor(R.color.colorWantToRead),
            ReadingStatus.READING to context.getColor(R.color.colorReading),
            ReadingStatus.READ to context.getColor(R.color.colorRead),
            ReadingStatus.ABANDONED to context.getColor(R.color.colorAbandoned)
        )
        
        // Dibujar gráfico de barras horizontal en lugar de circular para mejor legibilidad
        val barHeight = height / 4f
        val barWidth = width * 0.7f
        val startX = (width - barWidth) / 2f
        val startY = height / 2f - barHeight * 1.5f
        
        var yOffset = 0f
        val statusOrder = listOf(
            ReadingStatus.WANT_TO_READ,
            ReadingStatus.READING,
            ReadingStatus.READ,
            ReadingStatus.ABANDONED
        )
        
        statusOrder.forEachIndexed { index, status ->
            val count = stats[status] ?: 0
            val percentage = if (total > 0) (count.toFloat() / total) else 0f
            val barLength = barWidth * percentage
            
            // Dibujar barra
            paint.color = colors[status] ?: context.getColor(R.color.colorPrimary)
            val barTop = startY + yOffset
            val barBottom = barTop + barHeight * 0.6f
            canvas.drawRect(
                startX,
                barTop,
                startX + barLength,
                barBottom,
                paint
            )
            
            // Dibujar etiqueta
            val labelY = barTop + barHeight * 0.3f
            val statusLabel = when (status) {
                ReadingStatus.WANT_TO_READ -> context.getString(R.string.want_to_read)
                ReadingStatus.READING -> context.getString(R.string.reading)
                ReadingStatus.READ -> context.getString(R.string.read)
                ReadingStatus.ABANDONED -> context.getString(R.string.abandoned)
            }
            
            labelPaint.color = context.getColor(R.color.colorOnBackground)
            canvas.drawText(
                statusLabel,
                startX - 20f,
                labelY,
                labelPaint
            )
            
            // Dibujar valor
            textPaint.textSize = 28f
            textPaint.color = context.getColor(R.color.colorOnBackground)
            canvas.drawText(
                "$count",
                startX + barLength + 30f,
                labelY,
                textPaint
            )
            
            yOffset += barHeight
        }
        
        // Dibujar total en la parte inferior
        textPaint.textSize = 32f
        textPaint.color = context.getColor(R.color.colorPrimary)
        canvas.drawText(
            context.getString(R.string.total_books, total),
            centerX,
            height - 20f,
            textPaint
        )
    }
}

