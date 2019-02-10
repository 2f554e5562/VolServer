package utils

data class Token(
    val userId: Int,
    val userStamp: String
) {
    override fun toString() =
        "$userId:${userStamp.toUpperCase()}"
}

data class AuthUserData(
    val id: Int,
    val login: String,
    val password: String
)