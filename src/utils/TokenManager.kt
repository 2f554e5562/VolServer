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

    fun parseToken(token: String) =
        Token(token.split(":")[0].toLong(), token.split(":")[1])

    fun createRefreshToken(authUserData: AuthUserData): Token {
        val stamp =
            (authUserData.id.toString() +
                    authUserData.login +
                    authUserData.password +
                    SimpleDateFormat("yyyyMMddHHmm").format(Date())
                    ).getHashSHA256()

        return Token(authUserData.id, stamp)
    }
}
