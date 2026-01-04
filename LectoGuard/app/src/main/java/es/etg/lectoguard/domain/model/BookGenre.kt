package es.etg.lectoguard.domain.model

/**
 * Géneros literarios disponibles para clasificar libros
 */
enum class BookGenre(val displayName: String) {
    FICTION("Ficción"),
    NON_FICTION("No ficción"),
    SCIENCE_FICTION("Ciencia ficción"),
    FANTASY("Fantasía"),
    MYSTERY("Misterio"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    HISTORICAL("Histórica"),
    BIOGRAPHY("Biografía"),
    AUTOBIOGRAPHY("Autobiografía"),
    PHILOSOPHY("Filosofía"),
    PSYCHOLOGY("Psicología"),
    SELF_HELP("Autoayuda"),
    BUSINESS("Negocios"),
    TECHNOLOGY("Tecnología"),
    POETRY("Poesía"),
    DRAMA("Drama"),
    COMEDY("Comedia"),
    HORROR("Terror"),
    ADVENTURE("Aventura"),
    YOUNG_ADULT("Juvenil"),
    CHILDREN("Infantil"),
    CLASSIC("Clásico"),
    CONTEMPORARY("Contemporánea"),
    OTHER("Otro");
    
    companion object {
        fun fromString(value: String?): BookGenre {
            return try {
                valueOf(value ?: OTHER.name)
            } catch (e: Exception) {
                OTHER
            }
        }
        
        fun getAllDisplayNames(): List<String> = values().map { it.displayName }
    }
}

