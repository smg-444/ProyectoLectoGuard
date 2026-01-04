package es.etg.lectoguard.domain.model

/**
 * Estados de lectura para libros guardados por el usuario
 */
enum class ReadingStatus(val displayName: String) {
    WANT_TO_READ("Quiero leer"),
    READING("Leyendo"),
    READ("Leído"),
    ABANDONED("Abandonado");
    
    companion object {
        fun fromString(value: String?): ReadingStatus {
            return try {
                valueOf(value ?: WANT_TO_READ.name)
            } catch (e: Exception) {
                WANT_TO_READ
            }
        }
    }
}

