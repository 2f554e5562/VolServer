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

data class UsersProfileEditI(
    @JsonProperty("new_data") val newData: UserDataEdit
)

data class UsersProfileEditO(
    @JsonProperty("new_data") val newData: UserData
)


data class UserConstructorData(
    @JsonProperty("first_name") val firstName: String,
    @JsonProperty("last_name") val lastName: String,
    @JsonProperty("middle_name") val middleName: String,
    @JsonProperty("birthday") val birthday: Long,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phone_number") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("link") val link: String?
)

data class UserData(
    @JsonProperty("id") val id: Int,
    @JsonProperty("first_name") val firstName: String,
    @JsonProperty("last_name") val lastName: String,
    @JsonProperty("middle_name") val middleName: String,
    @JsonProperty("birthday") val birthday: Long,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phone_number") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("link") val link: String?
)

data class UserFullData(
    @JsonProperty("id") val id: Int = -1,
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("first_name") val firstName: String,
    @JsonProperty("last_name") val lastName: String,
    @JsonProperty("middle_name") val middleName: String,
    @JsonProperty("birthday") val birthday: Long,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phone_number") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("link") val link: String?
)

data class UserDataEdit(
    @JsonProperty("first_name") val firstName: String? = null,
    @JsonProperty("last_name") val lastName: String? = null,
    @JsonProperty("middle_name") val middleName: String? = null,
    @JsonProperty("birthday") val birthday: Long? = null,
    @JsonProperty("about") val about: String? = null,
    @JsonProperty("phone_number") val phoneNumber: String? = null,
    @JsonProperty("image") val image: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("link") val link: String? = null
)

data class UserDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
    @JsonProperty("first_name") val firstName: String? = null,
    @JsonProperty("last_name") val lastName: String? = null,
    @JsonProperty("middle_name") val middleName: String? = null,
    @JsonProperty("birthday_min") val birthdayMin: Long? = null,
    @JsonProperty("birthday_max") val birthdayMax: Long? = null,
    @JsonProperty("about") val about: String? = null,
    @JsonProperty("phone_number") val phoneNumber: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("link") val link: String? = null
)
