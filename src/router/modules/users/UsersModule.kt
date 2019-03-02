import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import utils.ErrorMessage

fun Routing.usersProfileGet(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/profile/get") {
        try {
            val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

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
                            UserData(
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

fun Routing.usersFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/find") {
        try {
            val usersFindI = json.readValue<UsersFindI>(call.receive<ByteArray>())

            val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

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

                    usersFindI.parameters.apply {
                        ids?.let { ids ->
                            query.andWhere { UsersTable.id inList ids }
                        }

                        firstName?.let { firstName ->
                            query.andWhere { UsersTable.firstName like "%$firstName%" }
                        }

                        lastName?.let { lastName ->
                            query.andWhere { UsersTable.lastName like "%$lastName%" }
                        }

                        middleName?.let { middleName ->
                            query.andWhere { UsersTable.middleName like "%$middleName%" }
                        }

                        birthdayMin?.let { birthdayMin ->
                            query.andWhere { UsersTable.birthday greaterEq birthdayMin }
                        }

                        birthdayMax?.let { birthdayMax ->
                            query.andWhere { UsersTable.birthday lessEq birthdayMax }
                        }

                        about?.let { about ->
                            query.andWhere { UsersTable.about like "%$about%" }
                        }

                        phoneNumber?.let { phoneNumber ->
                            query.andWhere { UsersTable.phoneNumber like "%$phoneNumber%" }
                        }

                        email?.let { email ->
                            query.andWhere { UsersTable.email like "%$email%" }
                        }

                        vkLink?.let { vkLink ->
                            query.andWhere { UsersTable.vkLink like "%$vkLink%" }
                        }
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        UsersFindO(
                            volDatabase.findUsersByParameters(query, usersFindI.offset, usersFindI.amount)
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
