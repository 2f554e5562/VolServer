import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import utils.ErrorMessage
import utils.copyToSuspend
import utils.writeValueAsString
import java.io.File

fun Routing.imageUpload() =
    post("/upload/image") {
        try {
            val uploadedFile = call.receiveStream()

            val file = File(
                "/home/nikita/Desktop/VolServer/files/uploaded/images/",
                "${System.currentTimeMillis()} ${uploadedFile.hashCode()}".getHashSHA256()
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