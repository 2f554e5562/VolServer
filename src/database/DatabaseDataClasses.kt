import com.fasterxml.jackson.annotation.JsonProperty

data class VolUser(
    @JsonProperty("id") val id: Int,
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("middleName") val middleName: String,
    @JsonProperty("about") val about: String,
    @JsonProperty("phoneNumber") val phoneNumber: String,
    @JsonProperty("image") val image: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("groupIds") val groupIds: ArrayList<Int>,
    @JsonProperty("vkLink") val vkLink: String
)