import java.text.SimpleDateFormat
import java.util.*

class TokenManager {
    fun createToken(authUserData: AuthUserData): Token {
        val stamp =
            (authUserData.id.toString() +
                    authUserData.login +
                    authUserData.password +
                    SimpleDateFormat("yyyyMMdd")
                        .format(Date())).getHashSHA256()

        return Token(authUserData.id, stamp)
    }

    fun parseToken(token: String): Token? {
        return try {
            Token(token.split(":")[0].toLong(), token.split(":")[1])
        } catch (e: Exception) {
            null
        }
    }

    fun createRefreshToken(authUserData: AuthUserData): Token {
        val stamp =
            (authUserData.id.toString() +
                    authUserData.login +
                    authUserData.password +
                    SimpleDateFormat("yyyyMMddHHmm").format(Date())
                    ).getHashSHA256()

        return Token(authUserData.id, stamp)
    }

    fun createCode(administratorPermission: Boolean, nodeId: Long, token: Token, targetUserId: Long): ApplyCode {
        val permission = if (administratorPermission) "A" else "U"
        val stamp =
            (permission +
                    nodeId.toString() +
                    token.toString() +
                    targetUserId.toString() +
                    SimpleDateFormat("yyyyMMdd")
                        .format(Date())).getHashSHA256()

        return ApplyCode(administratorPermission, nodeId, token.userId, stamp)
    }

    fun parseCode(token: String): ApplyCode? {
        return try {
            val administratorPermission = token.split(":")[0] == "A"
            val nodeId = token.split(":")[1].toLong()
            val authUserId = token.split(":")[2].toLong()
            val stamp = token.split(":")[3]

            ApplyCode(administratorPermission, nodeId, authUserId, stamp)
        } catch (e: Exception) {
            null
        }
    }
}
