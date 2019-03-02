import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import database.GroupAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll


fun Routing.groupsCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/create") {
        try {
            val groupCreateI = json.readValue<GroupCreateI>(call.receive<ByteArray>())

            val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

            val user = volDatabase.findUserById(token.userId)

            if (user == null) {
                respondUnauthorized()
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
                    respondCreated(
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
                    respondUnauthorized()
                }
            }
        } catch (e: GroupAlreadyExists) {
            respondConflict()
        } catch (e: Exception) {
            respondBadRequest()
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

            val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

            val user = volDatabase.findUserById(token.userId)

            if (user == null) {
                respondUnauthorized()
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

                    respondOk(
                        GroupsFindO(
                            volDatabase.findGroupsByParameters(query, groupFindI.offset, groupFindI.amount)
                        ).writeValueAsString()
                    )
                } else {
                    respondUnauthorized()
                }
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }
