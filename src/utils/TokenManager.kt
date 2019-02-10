package utils

import getHashSHA256
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

        return Token(1, stamp)
    }
}