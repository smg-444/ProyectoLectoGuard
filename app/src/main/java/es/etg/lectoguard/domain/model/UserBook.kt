package es.etg.lectoguard.domain.model
 
data class UserBook(
    val userId: Int,
    val bookId: Int,
    val savedDate: String
) 