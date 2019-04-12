import com.fasterxml.jackson.annotation.JsonProperty

data class ImageUploadO(
    @JsonProperty("image_link") val imageLink: String
)

data class ApplyCodeI(
    @JsonProperty("code") val code: String
)

data class ApplyCodeO(
    @JsonProperty("successful") val successful: Boolean
)

data class CreateCodeI(
    @JsonProperty("administrator") val administrator: Boolean,
    @JsonProperty("target_id") val targetId: Long,
    @JsonProperty("target_user_id") val targetUserId: Long
)

data class CreateCodeO(
    @JsonProperty("code") val code: String
)
