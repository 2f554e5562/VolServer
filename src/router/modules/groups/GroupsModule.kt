import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import database.GroupAlreadyExists
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import utils.AuthUserData
import utils.ErrorMessage
import utils.TokenManager
import utils.writeValueAsString


fun Routing.groupsCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
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


fun Routing.groupsFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
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