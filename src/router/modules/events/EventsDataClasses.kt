import com.fasterxml.jackson.annotation.JsonProperty

data class EventCreateI(
    @JsonProperty("data") val data: EventCreateData
)

data class EventCreateO(
    @JsonProperty("data") val data: EventFullData
)

data class EventsFindI(
    @JsonProperty("offset") val offset: Long,
    @JsonProperty("amount") val amount: Long,
    @JsonProperty("parameters") val parameters: EventDataSearch
)

data class EventsFindO(
    @JsonProperty("events") val events: List<EventFullData>
)

data class EventsEditI(
    @JsonProperty("id") val id: Long,
    @JsonProperty("new_data") val newData: EventsDataEdit
)

data class EventsEditO(
    @JsonProperty("new_data") val newData: EventFullData
)

data class EventsJoinI(
    @JsonProperty("event_id") val eventId: Long,
    @JsonProperty("state") val state: Boolean
)

data class EventsJoinO(
    @JsonProperty("successful") val successful: Boolean
)

data class EventsLikeI(
    @JsonProperty("event_id") val eventId: Long,
    @JsonProperty("state") val state: Boolean
)

data class EventsLikeO(
    @JsonProperty("successful") val successful: Boolean
)

data class EventsLikedI(
    @JsonProperty("offset") val offset: Long,
    @JsonProperty("amount") val amount: Long,
    @JsonProperty("user_id") val userId: Long? = null
)

data class EventsLikedO(
    @JsonProperty("events") val events: List<EventFullData>
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
    @JsonProperty("id") val id: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("creator_id") val creatorId: Long,
    @JsonProperty("place") val place: String?,
    @JsonProperty("datetime") val datetime: Long?,
    @JsonProperty("duration") val duration: Long?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("link") val link: String?,
    @JsonProperty("joined") val joined: Boolean?,
    @JsonProperty("liked") val liked: Boolean?
)

data class EventDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("creator_ids") val creatorIds: List<Int>? = null,
    @JsonProperty("place") val place: String? = null,
    @JsonProperty("datetime_min") val datetimeMin: Long? = null,
    @JsonProperty("datetime_max") val datetimeMax: Long? = null,
    @JsonProperty("duration_min") val durationMin: Long? = null,
    @JsonProperty("duration_max") val durationMax: Long? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("link") val link: String? = null
)

data class EventsDataEdit(
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("place") val place: String? = null,
    @JsonProperty("datetime") val datetime: Long? = null,
    @JsonProperty("duration") val duration: Long? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("link") val link: String? = null
)
