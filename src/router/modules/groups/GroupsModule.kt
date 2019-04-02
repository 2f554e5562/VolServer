import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import database.GroupAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post


fun Routing.groupsCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/create") {
        try {
            val groupCreateI = json.readValue<GroupCreateI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val group = volDatabase.createGroup(
                    groupCreateI.data, user.id
                )

                respondCreated(
                    GroupCreateO(
                        GroupFullData(
                            group.id.value,
                            group.title,
                            group.description,
                            group.canPost,
                            group.color,
                            group.image,
                            group.link,
                            group.creatorId
                        )
                    ).writeValueAsString()
                )
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
    post("/groups/list/find") {
        try {
            val groupFindI = json.readValue<GroupsFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    GroupsFindO(
                        volDatabase.findGroupsByParameters(groupFindI.parameters, groupFindI.offset, groupFindI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }


fun Routing.groupsEdit(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/{id}/edit") {
        try {
            val groupsEditI = json.readValue<GroupsEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val groupId = call.request.headers["id"]?.toInt()

                if (groupId != null) {
                    val groupData = volDatabase.editGroup(groupsEditI.newData, groupId)

                    if (groupData != null) {
                        respondOk(
                            GroupsEditO(
                                groupData
                            ).writeValueAsString()
                        )
                    } else {
                        respondNotFound()
                    }
                } else {
                    respondNotFound()
                }
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }
