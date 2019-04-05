data class Token(
    val userId: Long,
    val userStamp: String
) {
    override fun toString() =
        "$userId:${userStamp.toUpperCase()}"
}

data class ApplyCode(
    val administratorPermission: Boolean,
    val nodeId: Long,
    val userId: Long,
    val userStamp: String
) {
    override fun toString() =
        "${if (administratorPermission) "A" else "U"}:$nodeId:$userId:${userStamp.toUpperCase()}"
}

data class AuthUserData(
    val id: Long,
    val login: String,
    val password: String
)
