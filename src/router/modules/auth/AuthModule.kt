import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import database.UserAlreadyExists
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import utils.ErrorMessage

fun Routing.authTokenCreateByLoginAndPassword(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/auth/token/create/byLoginAndPassword") {
        try {
            val createTokenByLoginI = json.readValue<CreateTokenByLoginI>(call.receive<ByteArray>())

            val user = volDatabase.findByLoginAndPassword(
                createTokenByLoginI.login,
                createTokenByLoginI.password
            )

            if (user != null) {
                call.respond(
                    HttpStatusCode.OK,
                    CreateTokenByLoginO(
                        tokenManager.createToken(
                            AuthUserData(
                                user.id.value,
                                user.login,
                                user.password
                            )
                        ).toString()
                    ).writeValueAsString()
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage(
                        Messages.e401
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorMessage(
                    Messages.e400
                ).writeValueAsString()
            )
        }
    }

fun Routing.authTokenCreateByRefreshToken(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/auth/token/create/byRefreshToken") {
        try {
            val refreshToken =
                tokenManager.parseToken(call.request.headers["Refresh-Token"] ?: throw IllegalStateException())

            val user = volDatabase.findUserById(refreshToken.userId)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage(
                        Messages.e401
                    ).writeValueAsString()
                )
            } else {
                val userRefreshToken = tokenManager.createRefreshToken(
                    AuthUserData(
                        user.id.value,
                        user.login,
                        user.password
                    )
                )

                if (userRefreshToken.toString() == refreshToken.toString()) {
                    call.respond(
                        HttpStatusCode.OK,
                        CreateTokenByRefreshTokenO(
                            tokenManager.createToken(
                                AuthUserData(
                                    user.id.value,
                                    user.login,
                                    user.password
                                )
                            ).toString()
                        ).writeValueAsString()
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorMessage(
                            Messages.e401
                        ).writeValueAsString()
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorMessage(
                    Messages.e400
                ).writeValueAsString()
            )
        }
    }

fun Routing.authUsersCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/create") {
        try {
            val createUserI = json.readValue<UsersCreateI>(call.receive<ByteArray>())

            val user = volDatabase.createUser(
                createUserI
            )

            val token = tokenManager.createToken(
                AuthUserData(
                    user.id.value,
                    user.login,
                    user.password
                )
            )

            call.respond(
                HttpStatusCode.Created,
                UsersCreateO(
                    token.toString()
                ).writeValueAsString()
            )
        } catch (e: UserAlreadyExists) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorMessage(
                    Messages.e409
                ).writeValueAsString()
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorMessage(
                    Messages.e400
                ).writeValueAsString()
            )
        }
    }
