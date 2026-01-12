package es.etg.lectoguard.data.repository

import es.etg.lectoguard.data.local.BookDao
import es.etg.lectoguard.data.local.UserBookDao
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.remote.InterestService
import es.etg.lectoguard.data.remote.UserBookService
import es.etg.lectoguard.data.remote.FollowService
import es.etg.lectoguard.domain.model.BookGenre
import es.etg.lectoguard.domain.model.BookRecommendation
import es.etg.lectoguard.domain.model.UserInterests
import android.util.Log

class RecommendationRepository(
    private val bookDao: BookDao,
    private val userBookDao: UserBookDao,
    private val interestService: InterestService,
    private val userBookService: UserBookService,
    private val followService: FollowService? = null
) {
    /**
     * Calcula los intereses del usuario basándose en los libros que ha leído
     * Los géneros se obtienen de los libros mismos, no de asignación manual
     */
    suspend fun calculateUserInterests(userId: Int, firebaseUid: String): UserInterests {
        val userBooks = userBookDao.getAllUserBooks(userId)
        Log.d("RecommendationRepository", "Calculando intereses para userId=$userId, firebaseUid=$firebaseUid, total libros guardados=${userBooks.size}")
        
        // DEBUG: Verificar todos los libros en la base de datos
        val allUserBooks = userBookDao.getBooksByUser(userId)
        Log.d("RecommendationRepository", "DEBUG: Total libros para userId=$userId usando getBooksByUser: ${allUserBooks.size}")
        if (allUserBooks.isNotEmpty()) {
            Log.d("RecommendationRepository", "DEBUG: Primeros libros encontrados: ${allUserBooks.take(3).map { "bookId=${it.bookId}, status=${it.readingStatus}" }}")
        }
        
        // DEBUG: Verificar si hay libros con otros userIds (necesitamos acceso directo al DAO)
        try {
            // Intentar obtener todos los libros para debug
            val debugAllBooks = userBookDao.getAllBooks()
            Log.d("RecommendationRepository", "DEBUG: Total libros en toda la BD: ${debugAllBooks.size}")
            if (debugAllBooks.isNotEmpty()) {
                val userIds = debugAllBooks.map { it.userId }.distinct()
                Log.d("RecommendationRepository", "DEBUG: userIds encontrados en BD: $userIds")
                userIds.forEach { uid ->
                    val count = debugAllBooks.count { it.userId == uid }
                    Log.d("RecommendationRepository", "DEBUG: userId=$uid tiene $count libros guardados")
                }
            }
        } catch (e: Exception) {
            Log.e("RecommendationRepository", "DEBUG: Error obteniendo todos los libros: ${e.message}")
        }
        
        val genreCounts = mutableMapOf<BookGenre, Int>()
        var librosLeidosCount = 0
        var librosSinGeneros = 0
        
        userBooks.forEach { userBook ->
            // Solo contar libros leídos o en proceso de lectura para intereses
            if (userBook.readingStatus == es.etg.lectoguard.domain.model.ReadingStatus.READ ||
                userBook.readingStatus == es.etg.lectoguard.domain.model.ReadingStatus.READING) {
                
                librosLeidosCount++
                // Obtener el libro para acceder a sus géneros
                val book = bookDao.getBookById(userBook.bookId)
                if (book != null) {
                    if (book.genres.isNotEmpty()) {
                        // Contar cada género del libro
                        book.genres.forEach { genre ->
                            genreCounts[genre] = (genreCounts[genre] ?: 0) + 1
                        }
                        Log.d("RecommendationRepository", "Libro ${book.id} (${book.title}) tiene géneros: ${book.genres.map { it.name }}")
                    } else {
                        librosSinGeneros++
                        Log.w("RecommendationRepository", "Libro ${book.id} (${book.title}) NO tiene géneros asignados")
                    }
                } else {
                    Log.w("RecommendationRepository", "Libro con ID ${userBook.bookId} no encontrado en BD local")
                }
            }
        }
        
        Log.d("RecommendationRepository", "Resumen: $librosLeidosCount libros leídos/leyendo, $librosSinGeneros sin géneros, ${genreCounts.size} géneros únicos encontrados")
        Log.d("RecommendationRepository", "Géneros contados: ${genreCounts.map { "${it.key.name}=${it.value}" }.joinToString()}")
        
        val interests = UserInterests(
            userId = firebaseUid,
            genres = genreCounts
        )
        
        // Guardar intereses en Firestore
        try {
            interestService.saveUserInterests(interests)
            Log.d("RecommendationRepository", "Intereses guardados en Firestore correctamente")
        } catch (e: Exception) {
            Log.e("RecommendationRepository", "Error guardando intereses en Firestore: ${e.message}", e)
        }
        
        return interests
    }
    
    /**
     * Obtiene recomendaciones de libros basadas en intereses compartidos
     * @param userId ID local del usuario
     * @param firebaseUid UID de Firebase del usuario
     * @param limit Límite de recomendaciones a retornar
     * @param genreFilter Género específico para filtrar (null = todos los géneros)
     * @param onlyFromFollowing Si es true, solo recomienda libros de usuarios que sigue
     */
    suspend fun getRecommendations(
        userId: Int,
        firebaseUid: String,
        limit: Int = 10,
        genreFilter: BookGenre? = null,
        onlyFromFollowing: Boolean = false
    ): List<BookRecommendation> {
        try {
            // Calcular intereses actuales del usuario
            val userInterests = calculateUserInterests(userId, firebaseUid)
            
            if (userInterests.genres.isEmpty()) {
                Log.w("RecommendationRepository", "Usuario sin intereses calculados aún. Asegúrate de tener libros marcados como 'Leído' o 'Leyendo' con géneros asignados.")
                return emptyList()
            }
            
            Log.d("RecommendationRepository", "Intereses del usuario calculados: ${userInterests.genres.map { "${it.key.name}=${it.value}" }.joinToString()}")
            
            // Obtener usuarios seguidos si el filtro está activado
            val followingList = if (onlyFromFollowing && followService != null) {
                followService.getFollowingList(firebaseUid)
            } else {
                emptyList()
            }
            
            // Obtener todos los usuarios con intereses
            var allUsersInterests = interestService.getAllUsersWithInterests(firebaseUid)
            Log.d("RecommendationRepository", "Usuarios con intereses encontrados: ${allUsersInterests.size}")
            
            // Filtrar por usuarios seguidos si es necesario
            if (onlyFromFollowing && followingList.isNotEmpty()) {
                allUsersInterests = allUsersInterests.filter { it.userId in followingList }
                Log.d("RecommendationRepository", "Filtrando por usuarios seguidos: ${followingList.size} usuarios, quedan ${allUsersInterests.size} después del filtro")
            }
            
            // Si no hay otros usuarios con intereses, no podemos generar recomendaciones
            if (allUsersInterests.isEmpty()) {
                Log.w("RecommendationRepository", "No hay otros usuarios con intereses en Firestore. Las recomendaciones requieren al menos 2 usuarios.")
                return emptyList()
            }
            
            // Encontrar usuarios con intereses similares
            // Umbral dinámico: reducir umbral si hay pocos usuarios para permitir más recomendaciones
            val dynamicThreshold = when {
                allUsersInterests.size > 50 -> 0.4
                allUsersInterests.size > 10 -> 0.25
                allUsersInterests.size > 2 -> 0.15  // Umbral más bajo para pocos usuarios
                else -> 0.1  // Umbral muy bajo si solo hay 1-2 usuarios más
            }
            Log.d("RecommendationRepository", "Umbral de similitud dinámico: $dynamicThreshold (usuarios totales: ${allUsersInterests.size})")
            
            val similarUsers = allUsersInterests
                .map { otherInterests ->
                    val similarity = userInterests.calculateSimilarity(otherInterests)
                    Log.d("RecommendationRepository", "Similitud con usuario ${otherInterests.userId}: $similarity (géneros: ${otherInterests.genres.keys.map { it.name }.joinToString()})")
                    Pair(otherInterests.userId, similarity)
                }
                .filter { it.second > dynamicThreshold }
                .sortedByDescending { it.second }
                .take(30) // Top 30 usuarios más similares (aumentado para mejor cobertura)
            
            Log.d("RecommendationRepository", "Usuarios similares encontrados: ${similarUsers.size} (umbral: $dynamicThreshold)")
            similarUsers.take(5).forEach { (uid, sim) ->
                Log.d("RecommendationRepository", "  - Usuario $uid: similitud=$sim")
            }
            
            if (similarUsers.isEmpty()) {
                Log.w("RecommendationRepository", "No se encontraron usuarios con intereses similares (umbral: $dynamicThreshold)")
                Log.w("RecommendationRepository", "Sugerencia: Intenta reducir el umbral o asegúrate de que otros usuarios también tengan libros marcados como 'Leído' o 'Leyendo'")
                return emptyList()
            }
            
            // Obtener libros de usuarios similares que el usuario actual no tiene
            val userBookIds = userBookDao.getAllUserBooks(userId).map { it.bookId }.toSet()
            Log.d("RecommendationRepository", "Libros del usuario actual: ${userBookIds.size} libros")
            val recommendedBooks = mutableMapOf<Int, MutableList<Pair<String, Double>>>() // bookId -> List<(userId, similarity)>
            
            // Obtener libros de todos los usuarios similares en una sola consulta (más eficiente)
            val similarUserIds = similarUsers.map { it.first }
            val booksByUser = userBookService.getBooksFromUsers(similarUserIds)
            Log.d("RecommendationRepository", "Libros obtenidos de usuarios similares: ${booksByUser.values.sumOf { it.size }} libros totales")
            
            // Procesar libros de usuarios similares
            similarUsers.forEach { (similarUserId, similarity) ->
                val similarUserBooks = booksByUser[similarUserId] ?: emptyList()
                Log.d("RecommendationRepository", "Usuario $similarUserId tiene ${similarUserBooks.size} libros guardados")
                
                similarUserBooks.forEach { bookId ->
                    if (!userBookIds.contains(bookId)) {
                        if (!recommendedBooks.containsKey(bookId)) {
                            recommendedBooks[bookId] = mutableListOf()
                        }
                        recommendedBooks[bookId]?.add(Pair(similarUserId, similarity))
                    }
                }
            }
            
            Log.d("RecommendationRepository", "Libros candidatos para recomendación: ${recommendedBooks.size}")
            
            // Convertir a recomendaciones y calcular score mejorado
            val recommendations = recommendedBooks.map { (bookId, users) ->
                // Obtener información del libro
                val book = bookDao.getBookById(bookId)
                if (book == null) return@map null
                
                // Obtener géneros del libro (desde BookEntity, no de UserBook)
                val bookGenres = book.genres
                
                // Filtrar por género si se especificó
                if (genreFilter != null && !bookGenres.contains(genreFilter)) {
                    return@map null
                }
                
                // Score mejorado: combina similitud promedio con cantidad de recomendadores
                val avgSimilarity = users.map { it.second }.average()
                val maxSimilarity = users.map { it.second }.maxOrNull() ?: 0.0
                val userCount = users.size
                
                // Bonus adicional si viene de usuarios seguidos
                val followingBonus = if (onlyFromFollowing && followService != null) {
                    val followingCount = users.count { it.first in followingList }
                    if (followingCount > 0) 0.15 else 0.0 // Bonus del 15% si viene de seguidos
                } else {
                    0.0
                }
                
                // Ponderar: más usuarios similares = mejor recomendación
                // Pero también considerar la similitud máxima (el usuario más similar)
                val popularityBonus = kotlin.math.min(userCount / 10.0, 0.3) // Bonus hasta 30% por popularidad
                val maxSimilarityWeight = maxSimilarity * 0.4 // 40% del score viene del usuario más similar
                val avgSimilarityWeight = avgSimilarity * 0.3 // 30% del score viene del promedio
                
                val finalScore = (maxSimilarityWeight + avgSimilarityWeight + popularityBonus + followingBonus).coerceIn(0.0, 1.0)
                
                val reason = when {
                    onlyFromFollowing && users.any { it.first in followingList } -> {
                        val followingCount = users.count { it.first in followingList }
                        if (followingCount == 1) {
                            "Recomendado por un usuario que sigues"
                        } else {
                            "Recomendado por $followingCount usuarios que sigues"
                        }
                    }
                    userCount == 1 -> "Recomendado por un usuario con gustos muy similares"
                    userCount <= 3 -> "Recomendado por $userCount usuarios con gustos similares"
                    userCount <= 10 -> "Recomendado por $userCount usuarios con intereses afines"
                    else -> "Muy popular entre usuarios con gustos similares ($userCount usuarios)"
                }
                
                BookRecommendation(
                    bookId = bookId,
                    bookTitle = book.title,
                    bookCoverUrl = book.coverImage,
                    reason = reason,
                    similarityScore = finalScore,
                    recommendedBy = users.map { it.first },
                    genre = bookGenres.firstOrNull() // Usar el primer género como principal
                )
            }.filterNotNull()
                .sortedByDescending { it.similarityScore }
                .take(limit)
            
            Log.d("RecommendationRepository", "Generadas ${recommendations.size} recomendaciones")
            return recommendations
            
        } catch (e: Exception) {
            Log.e("RecommendationRepository", "Error obteniendo recomendaciones: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Obtiene los IDs de libros de un usuario similar desde Firestore
     */
    private suspend fun getBooksFromSimilarUser(userId: String): List<Int> {
        return userBookService.getUserBookIds(userId)
    }
    
}

