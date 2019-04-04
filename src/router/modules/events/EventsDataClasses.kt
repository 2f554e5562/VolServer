import com.fasterxml.jackson.annotation.JsonProperty

data class EventCreateI(
    @JsonProperty("data") val data: EventCreateData
)

data class EventCreateO(
    @JsonProperty("data") val data: EventFullData
)

data class EventsFindI(
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("parameters") val parameters: EventDataSearch
)

data class EventsFindO(
    @JsonProperty("events") val events: List<EventFullData>
)

data class EventsFindByUserI(
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("relation") val relation: Int
)

data class EventsFindByUserO(
    @JsonProperty("events") val events: List<EventFullData>
)

data class EventsEditI(
    @JsonProperty("new_data") val newData: EventsDataEdit
)

data class EventsEditO(
    @JsonProperty("new_data") val newData: EventFullData
)


data class EventCreateData(
    @JsonProperty("title") val title: String,
    @JsonProperty("place") val place: String?,
    @JsonProperty("datetime") val datetime: Long?,
    @JsonProperty("duration") val duration: Long?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("link") val link: String?
)

data class EventFullData(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("author_id") val authorId: Int,
    @JsonProperty("place") val place: String?,
    @JsonProperty("datetime") val datetime: Long?,
    @JsonProperty("duration") val duration: Long?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("link") val link: String?
)

data class EventDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("author_ids") val authorIds: List<Int>? = null,
    @JsonProperty("place") val place: String? = null,
    @JsonProperty("datetime_min") val datetimeMin: Long? = null,
    @JsonProperty("datetime_max") val datetimeMax: Long? = null,
    @JsonProperty("duration_min") val durationMin: Long? = null,
    @JsonProperty("duration_max") val durationMax: Long? = null,
    @JsonProperty("description") val description: String? = null
)

data class EventsDataEdit(
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("place") val place: String? = null,
    @JsonProperty("datetime") val datetime: Long? = null,
    @JsonProperty("duration") val duration: Long? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("link") val link: String? = null
)
