import com.fasterxml.jackson.annotation.JsonProperty

//data class UsersProfileGetI()

data class UsersProfileGetO(
    @JsonProperty("data") val data: UserData
)

data class UsersFindI(
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("parameters") val parameters: UserDataSearch
)

data class UsersFindO(
    @JsonProperty("users") val users: List<UserData>
)


data class UserData(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("middleName") val middleName: String,
    @JsonProperty("birthday") val birthday: Long,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phoneNumber") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("vkLink") val vkLink: String?
)

data class UserDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
    @JsonProperty("firstName") val firstName: String? = null,
    @JsonProperty("lastName") val lastName: String? = null,
    @JsonProperty("middleName") val middleName: String? = null,
    @JsonProperty("birthdayMin") val birthdayMin: Long? = null,
    @JsonProperty("birthdayMax") val birthdayMax: Long? = null,
    @JsonProperty("about") val about: String? = null,
    @JsonProperty("phoneNumber") val phoneNumber: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("vkLink") val vkLink: String? = null
)
