import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dataClasses.CreateTokenByLoginI
import dataClasses.CreateTokenByLoginO
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
        post("/auth/users/create") {
            try {
                val createUserI = json.readValue<CreateUserI>(call.receive<ByteArray>())

                val user = volDatabase.createUser(
                    createUserI
                )

                val token = tokenManager.createToken(AuthUserData(
                    user.id.value,
                    user.login,
                    user.password
                ))

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

        post("/auth/token/create/byLoginAndPassword") {
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
        }

        post("/auth/token/create/byRefreshToken") {
            call.respond(
                HttpStatusCode.OK,
                ""
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

