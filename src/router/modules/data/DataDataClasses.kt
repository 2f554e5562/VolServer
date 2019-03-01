package router.modules.data

import com.fasterxml.jackson.annotation.JsonProperty

data class ImageUploadO(
    @JsonProperty("file_name") val fileName: String
)