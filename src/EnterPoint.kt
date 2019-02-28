import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.*
import database.GroupAlreadyExists
import database.UserAlreadyExists
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import utils.*
import java.io.File

val volDatabase = DatabaseModule()
val tokenManager = TokenManager()
val json = jacksonObjectMapper()

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    volDatabase.connect()

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

        post("/groups/create") {
            try {
                val groupCreateI = json.readValue<GroupCreateI>(call.receive<ByteArray>())

                val token = tokenManager.parseToken(groupCreateI.token)

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
                        val group = volDatabase.createGroup(
                            groupCreateI.data, user.id.value
                        )

                        call.respond(
                            HttpStatusCode.Created,
                            GroupCreateO(
                                GroupData(
                                    group.title,
                                    group.description,
                                    group.canPost,
                                    group.color,
                                    group.image,
                                    group.vkLink,
                                    group.creatorId
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
            } catch (e: GroupAlreadyExists) {
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

        post("/groups/find") {
            try {
                val groupFindI = json.readValue<GroupsFindI>(call.receive<ByteArray>())

                val token = tokenManager.parseToken(groupFindI.token)

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
                        val query = GroupsTable.selectAll()

                        groupFindI.parameters.apply {
                            ids?.let { ids ->
                                query.andWhere { GroupsTable.id inList ids }
                            }
                            title?.let { title ->
                                query.andWhere { GroupsTable.title like "%$title%" }
                            }
                            description?.let { description ->
                                query.andWhere { GroupsTable.description like "%$description%" }
                            }
                            canPost?.let { canPost ->
                                query.andWhere { GroupsTable.canPost eq canPost }
                            }
                            vkLink?.let { vkLink ->
                                query.andWhere { GroupsTable.vkLink like "%$vkLink%" }
                            }
                            creatorIds?.let { creatorIds ->
                                query.andWhere { GroupsTable.creatorId inList creatorIds }
                            }
                        }

                        call.respond(
                            HttpStatusCode.OK,
                            GroupsFindO(
                                volDatabase.findGroupsByParameters(query, groupFindI.offset, groupFindI.amount)
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

        post("/users/upload/image") {
            try {
                val uploadedFile = call.receiveStream()

                val file = File(
                    "/home/nikita/Desktop/VolServer/files/users/images/",
                    "${System.currentTimeMillis()} ${uploadedFile.hashCode()}".getHashSHA256() + ".jpg"
                )

                file.outputStream().buffered().use { output ->
                    uploadedFile.copyToSuspend(output)
                }

                call.respond(
                    HttpStatusCode.OK
                )
            } catch (error: Exception) {
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
