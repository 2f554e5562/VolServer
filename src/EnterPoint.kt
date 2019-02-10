import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.*
import database.UserAlreadyExists
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import utils.AuthUserData
import utils.TokenManager

val volDatabase = DatabaseModule()
val tokenManager = TokenManager()
val json = jacksonObjectMapper()

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = true) {
    volDatabase.connect()
    volDatabase.initUserTable()

    routing {
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

        post("/auth/token/create/byRefreshToken") {
            try {
                val createTokenByRefreshTokenI =
                    json.readValue<CreateTokenByRefreshTokenI>(call.receive<ByteArray>())

                val refreshToken = tokenManager.parseToken(createTokenByRefreshTokenI.refreshToken)

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

        post("/auth/users/create") {
            try {
                val createUserI = json.readValue<CreateUserI>(call.receive<ByteArray>())

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
                    HttpStatusCode.OK,
                    CreateUserO(
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

        post("/auth/users/profile/get") {
            try {
                val usersProfileGetI = json.readValue<UsersProfileGetI>(call.receive<ByteArray>())

                val token = tokenManager.parseToken(usersProfileGetI.token)

                val user = volDatabase.findUserById(token.userId)

                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorMessage(
                            Messages.e401
                        ).writeValueAsString()
                    )
                } else {
                    val userToken = tokenManager.createToken(
                        AuthUserData(
                            user.id.value,
                            user.login,
                            user.password
                        )
                    )

                    if (token.toString() == userToken.toString()) {
                        if (usersProfileGetI.id == null || token.userId == usersProfileGetI.id) {
                            call.respond(
                                HttpStatusCode.OK,
                                UsersProfileGetO(
                                    user.firstName,
                                    user.lastName,
                                    user.middleName,
                                    user.about,
                                    user.phoneNumber,
                                    user.image,
                                    user.email,
                                    user.vkLink
                                ).writeValueAsString()
                            )
                        } else {
                            val data = volDatabase.findUserById(usersProfileGetI.id)

                            if (data != null) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    UsersProfileGetO(
                                        data.firstName,
                                        data.lastName,
                                        data.middleName,
                                        data.about,
                                        data.phoneNumber,
                                        data.image,
                                        data.email,
                                        data.vkLink
                                    ).writeValueAsString()
                                )

                            } else {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorMessage(
                                        Messages.e404
                                    ).writeValueAsString()
                                )
                            }
                        }
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

        post("example") {
            try {
                //TODO inputDataI -> ..., String -> ...
                val inputDataI = json.readValue<UsersProfileGetI>(call.receive<ByteArray>())

                val token = tokenManager.parseToken(inputDataI.token)

                val user = volDatabase.findUserById(token.userId)

                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorMessage(
                            Messages.e401
                        ).writeValueAsString()
                    )
                } else {
                    val userToken = tokenManager.createToken(
                        AuthUserData(
                            user.id.value,
                            user.login,
                            user.password
                        )
                    )

                    if (token.toString() == userToken.toString()) {
                        //TODO Work...
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
    }
}