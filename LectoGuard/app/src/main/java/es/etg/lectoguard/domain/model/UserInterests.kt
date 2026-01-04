package es.etg.lectoguard.domain.model

/**
 * Modelo para los intereses de lectura de un usuario
 * Basado en los géneros de los libros que ha guardado/leído
 */
data class UserInterests(
    val userId: String,
    val genres: Map<BookGenre, Int> = emptyMap(), // Género -> cantidad de libros
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Obtiene los géneros principales del usuario (top N)
     */
    fun getTopGenres(limit: Int = 5): List<BookGenre> {
        return genres.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    /**
     * Calcula la similitud con otro usuario basado en intereses compartidos
     * Usa una combinación mejorada de similitud de coseno y Jaccard ponderado
     */
    fun calculateSimilarity(other: UserInterests): Double {
        if (genres.isEmpty() || other.genres.isEmpty()) return 0.0
        
        val allGenres = (genres.keys + other.genres.keys).toSet()
        if (allGenres.isEmpty()) return 0.0
        
        val commonGenres = genres.keys.intersect(other.genres.keys)
        if (commonGenres.isEmpty()) return 0.0
        
        // 1. Similitud de Coseno (mejor para datos dispersos)
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        
        allGenres.forEach { genre ->
            val myCount = (genres[genre] ?: 0).toDouble()
            val otherCount = (other.genres[genre] ?: 0).toDouble()
            
            dotProduct += myCount * otherCount
            normA += myCount * myCount
            normB += otherCount * otherCount
        }
        
        val cosineSimilarity = if (normA > 0 && normB > 0) {
            dotProduct / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
        } else {
            0.0
        }
        
        // 2. Coeficiente de Jaccard mejorado (ponderado por cantidad)
        var jaccardNumerator = 0.0
        var jaccardDenominator = 0.0
        
        commonGenres.forEach { genre ->
            val myCount = (genres[genre] ?: 0).toDouble()
            val otherCount = (other.genres[genre] ?: 0).toDouble()
            
            // Ponderar por el mínimo (géneros donde ambos tienen interés)
            val minCount = minOf(myCount, otherCount)
            val maxCount = maxOf(myCount, otherCount)
            
            jaccardNumerator += minCount
            jaccardDenominator += maxCount
        }
        
        // Sumar géneros únicos de cada usuario
        val uniqueToMe = genres.keys - other.genres.keys
        val uniqueToOther = other.genres.keys - genres.keys
        
        uniqueToMe.forEach { genre ->
            jaccardDenominator += (genres[genre] ?: 0).toDouble()
        }
        
        uniqueToOther.forEach { genre ->
            jaccardDenominator += (other.genres[genre] ?: 0).toDouble()
        }
        
        val jaccardSimilarity = if (jaccardDenominator > 0) {
            jaccardNumerator / jaccardDenominator
        } else {
            0.0
        }
        
        // 3. Factor de diversidad (más géneros en común = mayor similitud)
        val diversityFactor = commonGenres.size.toDouble() / allGenres.size.toDouble()
        
        // 4. Factor de intensidad (qué tan fuerte es el interés compartido)
        val totalMyBooks = genres.values.sum().toDouble()
        val totalOtherBooks = other.genres.values.sum().toDouble()
        val sharedBooks = commonGenres.sumOf { genre ->
            minOf(genres[genre] ?: 0, other.genres[genre] ?: 0).toDouble()
        }
        
        val intensityFactor = if (totalMyBooks > 0 && totalOtherBooks > 0) {
            sharedBooks / maxOf(totalMyBooks, totalOtherBooks)
        } else {
            0.0
        }
        
        // Combinar todas las métricas con pesos
        // Coseno: 40%, Jaccard: 30%, Diversidad: 20%, Intensidad: 10%
        val finalSimilarity = (
            cosineSimilarity * 0.4 +
            jaccardSimilarity * 0.3 +
            diversityFactor * 0.2 +
            intensityFactor * 0.1
        )
        
        // Asegurar que el resultado esté entre 0 y 1
        return finalSimilarity.coerceIn(0.0, 1.0)
    }
}

