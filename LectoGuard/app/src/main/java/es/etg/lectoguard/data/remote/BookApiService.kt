package es.etg.lectoguard.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class BookDetailResponse(
    val id: Int = 0,
    val title: String = "",
    val coverImage: String = "",
    val sinopsis: String = "",
    val firstPage: String = "",
    val genres: List<String> = emptyList() // Géneros del libro desde Realtime Database
)

interface BookApiService {
    @GET("libros/{id}/sinopsis.json")
    suspend fun getBookSynopsis(@Path("id") id: Int): Response<String>

    @GET("libros/{id}.json")
    suspend fun getBookDetail(@Path("id") id: Int): Response<BookDetailResponse>
    
    // Realtime Database devuelve arrays como [null, {...}, {...}, ...]
    // Usamos List<Any?> porque Realtime Database devuelve un array JSON directamente
    @GET("libros.json")
    suspend fun getAllBooks(): Response<List<Any?>>
}