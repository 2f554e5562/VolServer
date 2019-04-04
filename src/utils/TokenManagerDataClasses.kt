data class Token(
    val userId: Long,
    val userStamp: String
) {
    override fun toString() =
        "$userId:${userStamp.toUpperCase()}"
}

data class AuthUserData(
    val id: Long,
    val login: String,
    val password: String
)
