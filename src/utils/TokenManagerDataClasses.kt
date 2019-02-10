package utils

data class Token(
    val userId: Int,
    val userStamp: String
) {
    override fun toString() =
        "$userId:$userStamp"
}