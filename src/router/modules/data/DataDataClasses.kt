import com.fasterxml.jackson.annotation.JsonProperty

data class ImageUploadO(
    @JsonProperty("file_name") val fileName: String
)

data class ImageLoadI(
    @JsonProperty("file_name") val fileName: String
)