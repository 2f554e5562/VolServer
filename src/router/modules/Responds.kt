import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.respondBadRequest() =
    call.respond(
        HttpStatusCode.BadRequest,
        ErrorMessage(
            Messages.e400
        ).writeValueAsString()
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondConflict() =
    call.respond(
        HttpStatusCode.Conflict,
        ErrorMessage(
            Messages.e409
        ).writeValueAsString()
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondUnauthorized() =
    call.respond(
        HttpStatusCode.Unauthorized,
        ErrorMessage(
            Messages.e401
        ).writeValueAsString()
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondForbidden() =
    call.respond(
        HttpStatusCode.Forbidden,
        ErrorMessage(
            Messages.e403
        ).writeValueAsString()
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondOk(message: String) =
    call.respond(
        HttpStatusCode.OK,
        message
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondNotFound() =
    call.respond(
        HttpStatusCode.NotFound,
        ErrorMessage(
            Messages.e404
        ).writeValueAsString()
    )

suspend fun PipelineContext<Unit, ApplicationCall>.respondCreated(message: String = "") =
    call.respond(
        HttpStatusCode.Created,
        message
    )