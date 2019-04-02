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
                            event.id.value,
                            event.title,
                            event.authorId,
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
            respondBadRequest()
        }
    }


fun Routing.eventsFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
        post("/events/list/get") {
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
            respondBadRequest()
        }
    }

fun Routing.eventsEdit(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/{id}/edit") {
        try {
            val eventsEditI = json.readValue<EventsEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val eventId = call.request.headers["id"]?.toInt()

                if (eventId != null) {
                    val eventData = volDatabase.editEvent(eventsEditI.newData, eventId)

                    if (eventData != null) {
                        respondOk(
                            EventsEditO(
                                eventData
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

fun Routing.eventsFindByUser(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/events/list/get/byUser") {
        try {
            val eventsFindI = json.readValue<EventsFindByUserI>(call.receive<ByteArray>())

            val relation = when (eventsFindI.relation) {
                0 -> "any"
                1 -> "creatorOf"
                2 -> "participantOf"

                else -> throw IllegalStateException("Incorrect user -> event relation")
            }

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    EventsFindByUserO(
                        volDatabase.findEventsByUser(eventsFindI.userId, eventsFindI.offset, eventsFindI.amount, relation)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

