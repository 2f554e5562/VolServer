import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.response.respondBytes
import io.ktor.routing.Routing
import io.ktor.routing.post
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

            respondOk(
                ImageUploadO(
                    file.name
                ).writeValueAsString()
            )
        } catch (error: Exception) {
            respondBadRequest()
        }
    }

fun Routing.imageLoad(json: ObjectMapper) =
    post("/load/image") {
        try {
            val imageLoadI = json.readValue<ImageLoadI>(call.receive<ByteArray>())

            val file = File(
                "/home/nikita/Desktop/VolServer/files/uploaded/images/",
                imageLoadI.fileName
            )

            if (!file.exists()) {
                call.respondBytes(
                    file.readBytes(),
                    ContentType.Image.Any
                )
            }

            call.respondBytes(
                file.readBytes(),
                ContentType.Image.Any
            )
        } catch (error: Exception) {
            respondBadRequest()
        }
    }
