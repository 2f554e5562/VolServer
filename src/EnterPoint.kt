import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.CreateTokenByLoginI
import dataClasses.CreateUserI
import dataClasses.CreateUserO
import database.UserAlreadyExists
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
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
        post("/auth/users/create") {
            try {
                val createUserI = json.readValue<CreateUserI>(call.receive<ByteArray>())

                val userId = volDatabase.createUser(
                    createUserI
                )

                val token = tokenManager.createToken(userId)

                call.respond(
                    HttpStatusCode.OK,
                    CreateUserO(
                        token.toString()
                    ).writeValueAsString()
                )
            } catch (e: JsonParseException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        Messages.e400
                    ).writeValueAsString()
                )
            } catch (e: MissingKotlinParameterException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        Messages.e400
                    ).writeValueAsString()
                )
            } catch (e: UserAlreadyExists) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorMessage(
                        Messages.e409
                    ).writeValueAsString()
                )
            }
        }

        post("/auth/token/create/byLogin") {
            val createTokenByLoginI = json.readValue<CreateTokenByLoginI>(call.receive<ByteArray>())

            volDatabase.

            call.respond(
                HttpStatusCode.OK,
                CreateUserO(
                    tokenManager.createToken(1).toString()
                ).writeValueAsString()
            )
        }

        post("/auth/token/create/byRefreshToken") {
            call.respond(
                HttpStatusCode.OK,
                CreateUserO(
                    tokenManager.createToken(1).toString()
                ).writeValueAsString()
            )
        }

        post("example") {
            try {
                val createUserI = json.readValue<CreateUserI>(call.receive<ByteArray>())

                call.respond(
                    HttpStatusCode.OK,
                    ""
                )
            } catch (e: JsonParseException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        Messages.e400
                    ).writeValueAsString()
                )
            } catch (e: MissingKotlinParameterException) {
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

