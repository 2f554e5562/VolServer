import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class AuthData(
    val login: String,
    val password: String
)

data class ErrorMessage(
    val message: String
)

fun Any.writeValueAsString() =
    jacksonObjectMapper().writeValueAsString(this)