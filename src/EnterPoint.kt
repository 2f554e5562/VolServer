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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notLike
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import utils.AuthUserData
import utils.ErrorMessage
import utils.TokenManager
import utils.writeValueAsString

val volDatabase = DatabaseModule()
val tokenManager = TokenManager()
val json = jacksonObjectMapper()

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
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

        post("/users/create") {
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

        post("/users/profile/get") {
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
                        call.respond(
                            HttpStatusCode.OK,
                            UsersProfileGetO(
                                UserProfile(
                                    user.id.value,
                                    user.firstName,
                                    user.lastName,
                                    user.middleName,
                                    user.birthday,
                                    user.about,
                                    user.phoneNumber,
                                    user.image,
                                    user.email,
                                    user.vkLink
                                )
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

        post("/users/find") {
            try {
                val usersFindI = json.readValue<UsersFindI>(call.receive<ByteArray>())

                val token = tokenManager.parseToken(usersFindI.token)

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
                        val query = UsersTable.selectAll()

                        if (usersFindI.parameters.ids != null)
                            query.andWhere { UsersTable.id inList usersFindI.parameters.ids }

                        if (usersFindI.parameters.firstName != null)
                            query.andWhere { UsersTable.firstName like "%${usersFindI.parameters.firstName}%" }

                        if (usersFindI.parameters.lastName != null)
                            query.andWhere { UsersTable.lastName like "%${usersFindI.parameters.lastName}%" }

                        if (usersFindI.parameters.middleName != null)
                            query.andWhere { UsersTable.middleName like "%${usersFindI.parameters.middleName}%" }

                        if (usersFindI.parameters.birthdayMin != null)
                            query.andWhere { UsersTable.birthday greaterEq usersFindI.parameters.birthdayMin }

                        if (usersFindI.parameters.birthdayMax != null)
                            query.andWhere { UsersTable.birthday lessEq usersFindI.parameters.birthdayMax }

                        if (usersFindI.parameters.about != null)
                            query.andWhere { UsersTable.about like "%${usersFindI.parameters.about}%" }

                        if (usersFindI.parameters.phoneNumber != null)
                            query.andWhere { UsersTable.phoneNumber like "%${usersFindI.parameters.phoneNumber}%" }

                        if (usersFindI.parameters.email != null)
                            query.andWhere { UsersTable.email like "%${usersFindI.parameters.email}%" }

                        if (usersFindI.parameters.vkLink != null)
                            query.andWhere { UsersTable.vkLink like "%${usersFindI.parameters.vkLink}%" }

                        val users = volDatabase.findUsersByParameters(query, usersFindI.offset, usersFindI.amount)

                        call.respond(
                            HttpStatusCode.OK,
                            UsersFindO(
                                users
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