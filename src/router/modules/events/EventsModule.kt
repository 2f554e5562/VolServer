import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import java.lang.IllegalStateException

fun Routing.eventsCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/create") {
        try {
            val eventCreateI = json.readValue<EventCreateI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val event = volDatabase.createEvent(
                    eventCreateI.data, user.id
                )

                respondCreated(
                    EventCreateO(
                        EventFullData(
                            event.id,
                            event.title,
                            user.id,
                            event.place,
                            event.datetime,
                            event.duration,
                            event.description,
                            event.link
                        )
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.eventsFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
        post("/events/list/find") {
        try {
            val eventsFindI = json.readValue<EventsFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    EventsFindO(
                        volDatabase.findEventsByParameters(eventsFindI.parameters, eventsFindI.offset, eventsFindI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

fun Routing.eventsEdit(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/edit") {
        try {
            val eventsEditI = json.readValue<EventsEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->

                val eventData = volDatabase.editEvent(eventsEditI.newData, eventsEditI.id)

                if (eventData != null) {
                    respondOk(
                        EventsEditO(
                            eventData
                        ).writeValueAsString()
                    )
                } else {
                    respondNotFound()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.eventsJoin(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/join") {
        try {
            val eventsJoinI = json.readValue<EventsJoinI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = true

                volDatabase.joinEvent(eventsJoinI.eventId, user.id)

                if (successful) {
                    respondOk(
                        GroupsJoinO(
                            "Successful"
                        ).writeValueAsString()
                    )
                } else {
                    respondNotFound()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.eventsLeave(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/leave") {
        try {
            val eventsLeaveI = json.readValue<EventsLeaveI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = true

                volDatabase.leaveEvent(eventsLeaveI.eventId, user.id)

                if (successful) {
                    respondOk(
                        GroupsLeaveO(
                            "Successful"
                        ).writeValueAsString()
                    )
                } else {
                    respondNotFound()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }
