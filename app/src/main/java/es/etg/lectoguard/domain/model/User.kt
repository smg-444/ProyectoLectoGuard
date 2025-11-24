package es.etg.lectoguard.domain.model

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val signupDate: String
) 