package utils

import AuthData

class TokenManager {
    fun createToken(authData: AuthData): Token? {
        val token = Token(1, "A654S64D6H48M4L654A65U4K1V32H13A13R21S3L1M51D3J51C351A35S3J46D54C6JASL")

        return token
    }

    fun createToken(id: Int): Token? {
        val token = Token(1, "A654S64D6H48M4L654A65U4K1V32H13A13R21S3L1M51D3J51C351A35S3J46D54C6JASL")
        return token
    }
}