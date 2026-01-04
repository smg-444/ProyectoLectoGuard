package es.etg.lectoguard.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}m"
            hours < 24 -> "Hace ${hours}h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace ${days}d"
            else -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                val today = Calendar.getInstance()
                
                when {
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                        // Mismo año, mostrar día y mes
                        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
                    }
                    else -> {
                        // Año diferente, mostrar fecha completa
                        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
                    }
                }
            }
        }
    }
    
    fun formatMessageTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}m"
            hours < 24 -> {
                // Mostrar hora si es hoy
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            days == 1L -> "Ayer ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
            days < 7 -> {
                // Mostrar día de la semana y hora
                SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                // Mostrar fecha completa
                SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}

