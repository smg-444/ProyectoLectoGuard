package es.etg.lectoguard.domain.model
 
data class UserBook(
    val userId: Int,
    val bookId: Int,
    val savedDate: String,
    val readingStatus: ReadingStatus = ReadingStatus.WANT_TO_READ,
    val tags: List<String> = emptyList()
) 