import com.fasterxml.jackson.module.kotlin.readValue
import database.UserAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import router.json
import router.tokenManager
import router.volDatabase

fun Routing.authTokenCreateByLoginAndPassword() =
    post("/auth/token/create/byLoginAndPassword") {
        try {
            val createTokenByLoginI = json.readValue<CreateTokenByLoginI>(call.receive<ByteArray>())

            val user = volDatabase.findByLoginAndPassword(
                createTokenByLoginI.login,
                createTokenByLoginI.password
            )

            if (user != null) {
                respondOk(
                    CreateTokenByLoginO(
                        tokenManager.createToken(
                            AuthUserData(
                                user.id,
                                user.login,
                                user.password
                            )
                        ).toString()
                    ).writeValueAsString()
                )
            } else {
                respondUnauthorized()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

fun Routing.authTokenCreateByRefreshToken() =
    post("/auth/token/create/byRefreshToken") {
        try {
            val createTokenByRefreshTokenI = json.readValue<CreateTokenByRefreshTokenI>(call.receive<ByteArray>())

            val refreshToken = tokenManager.parseToken(createTokenByRefreshTokenI.refreshToken)

            if (refreshToken != null) {
                val user = volDatabase.findUserById(refreshToken.userId)

                if (user == null) {
                    respondUnauthorized()
                } else {
                    val userRefreshToken = tokenManager.createRefreshToken(
                        AuthUserData(
                            user.id,
                            user.login,
                            user.password
                        )
                    )

                    if (userRefreshToken.toString() == refreshToken.toString()) {
                        respondOk(
                            CreateTokenByRefreshTokenO(
                                tokenManager.createToken(
                                    AuthUserData(
                                        user.id,
                                        user.login,
                                        user.password
                                    )
                                ).toString()
                            ).writeValueAsString()
                        )
                    } else {
                        respondUnauthorized()
                    }
                }
            } else {
                respondUnauthorized()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

fun Routing.authUsersCreate() =
    post("/users/create") {
        try {
            val createUserI = json.readValue<UsersCreateI>(call.receive<ByteArray>())

            val user = volDatabase.createUser(
                createUserI
            )

            respondCreated(
                UsersCreateO(
                    tokenManager.createToken(
                        AuthUserData(
                            user.id,
                            user.login,
                            user.password
                        )
                    ).toString()
                ).writeValueAsString()
            )
        } catch (e: UserAlreadyExists) {
            respondConflict()
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }
