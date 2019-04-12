import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

val confFile = jacksonObjectMapper().readValue<Configuration>(File("configuration").readBytes())

data class Configuration(
    @JsonProperty("graph_server_url") val graphServerUrl: String,
    @JsonProperty("graph_server_user") val graphServerUser: String,
    @JsonProperty("graph_server_password") val graphServerPassword: String,
    @JsonProperty("image_server_url") val imageServerUrl: String
)