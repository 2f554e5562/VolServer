import database.GroupAlreadyExists
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseModule {
    private val volGraphDatabase = VolGraphDatabase

    fun createUser(user: UsersCreateI): UserNode {
        val users = volGraphDatabase.findNode<UserNode> {
            login = user.login.trimAllSpaces()
        }

        if (users.isEmpty())
            return volGraphDatabase.newNode<UserNode> {
                login = user.login.trimAllSpaces()
                password = user.password.trimAllSpaces()
                firstName = user.data.firstName.trimAllSpaces()
                lastName = user.data.lastName.trimAllSpaces()
                middleName = user.data.middleName.trimAllSpaces()
                birthday = user.data.birthday
                about = user.data.about?.trimAllSpaces()
                phoneNumber = user.data.phoneNumber?.trimAllSpaces()
                image = user.data.image?.trimAllSpaces() ?: "default"
                email = user.data.email?.trimAllSpaces()
                link = user.data.link?.trimAllSpaces()
            }
        else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String): UserNode? {
        return volGraphDatabase.findNode<UserNode> {
            this.login = login
            this.password = password
        }.firstOrNull()
    }

    fun findUserById(id: Long): UserNode? {
        return volGraphDatabase.findNode<UserNode> {
            this.id = id
        }.firstOrNull()
    }

    fun editUser(user: UserDataEdit, userId: Long): UserData? {
        return volGraphDatabase.editNode<UserNode>(userId) {
            user.firstName?.let { firstName = it }
            user.lastName?.let { lastName = it }
            user.middleName?.let { middleName = it }
            user.birthday?.let { birthday = it }

            about = user.about
            phoneNumber = user.phoneNumber
            image = user.image
            email = user.email
            link = user.link
        }.firstOrNull()?.let {
            UserData(
                it.id,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image,
                it.email,
                it.link
            )
        }
    }

    fun findUsersByParameters(parameters: UserDataSearch, offset: Int, amount: Int) = transaction {
        val query = UsersTable.selectAll()

        parameters.apply {
            ids?.let { ids ->
                query.andWhere { UsersTable.id inList ids }
            }

            firstName?.let { firstName ->
                query.andWhere { UsersTable.firstName like "%$firstName%" }
            }

            lastName?.let { lastName ->
                query.andWhere { UsersTable.lastName like "%$lastName%" }
            }

            middleName?.let { middleName ->
                query.andWhere { UsersTable.middleName like "%$middleName%" }
            }

            birthdayMin?.let { birthdayMin ->
                query.andWhere { UsersTable.birthday greaterEq birthdayMin }
            }

            birthdayMax?.let { birthdayMax ->
                query.andWhere { UsersTable.birthday lessEq birthdayMax }
            }

            about?.let { about ->
                query.andWhere { UsersTable.about like "%$about%" }
            }

            phoneNumber?.let { phoneNumber ->
                query.andWhere { UsersTable.phoneNumber like "%$phoneNumber%" }
            }

            email?.let { email ->
                query.andWhere { UsersTable.email like "%$email%" }
            }

            link?.let { vkLink ->
                query.andWhere { UsersTable.link like "%$vkLink%" }
            }
        }

        query.limit(amount, offset).map {
            UserData(
                it[UsersTable.id].value.toLong(),
                it[UsersTable.firstName],
                it[UsersTable.lastName],
                it[UsersTable.middleName],
                it[UsersTable.birthday],
                it[UsersTable.about],
                it[UsersTable.phoneNumber],
                it[UsersTable.image],
                it[UsersTable.email],
                it[UsersTable.link]
            )
        }
    }

    fun createGroup(group: GroupCreateData, userId: Long): GroupNode {
        val groups = volGraphDatabase.findNode<GroupNode> {
            title = group.title.trimAllSpaces()
        }

        if (groups.isEmpty()) {
            val groupNode = volGraphDatabase.newNode<GroupNode> {
                group.description?.let { description = it }
                group.color?.let { color = it }
                group.image?.let { image = it }
                group.link?.let { link = it }

                title = group.title.trimAllSpaces()
                creatorId = userId
            }

            volGraphDatabase.newRelation<CreatorRelationship>(userId, groupNode)

            return groupNode
        } else {
            throw GroupAlreadyExists()
        }
    }

    fun editGroup(group: GroupDataEdit, groupId: Long): GroupFullData? {
        return volGraphDatabase.editNode<GroupNode>(groupId) {
            group.title?.let { title = it.trimAllSpaces() }
            group.description?.let { description = it.trimAllSpaces() }
            group.color?.let { color = it.trimAllSpaces() }
            group.image?.let { image = it.trimAllSpaces() }
            group.link?.let { link = it.trimAllSpaces() }
        }.firstOrNull()?.let {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image,
                it.link,
                it.creatorId
            )
        }
    }

    fun findGroupsByParameters(parameters: GroupDataSearch, offset: Int, amount: Int): List<GroupFullData> {
        return volGraphDatabase.findNode<GroupNode> { filter ->
            parameters.ids?.let {
                filter.add { className ->
                    "ID($className)" inList it
                }
            }
            parameters.title?.let {
                filter.add { className ->
                    "$className.title" like it
                }
            }
            parameters.description?.let {
                filter.add { className ->
                    "$className.description" like it
                }
            }
            parameters.canPost?.let {
                filter.add { className ->
                    "$className.canPost" equals it
                }
            }
            parameters.link?.let {
                filter.add { className ->
                    "$className.link" like it
                }
            }
            parameters.creatorIds?.let {
                filter.add { className ->
                    "$className.creatorId" inList it
                }
            }
        }.map {
            GroupFullData(
                it.id,
                it.title,
                it.description,
                it.color,
                it.image,
                it.link,
                it.creatorId
            )
        }
    }

    fun createEvent(event: EventCreateData, userId: Long): EventNode {
        val events = volGraphDatabase.findNode<EventNode> {
            title = event.title.trimAllSpaces()
        }

        if (events.isEmpty()) {
            val eventNode = volGraphDatabase.newNode<EventNode> {
                title = event.title
                place = event.place
                datetime = event.datetime
                duration = event.duration
                description = event.description
                link = event.link
            }

            volGraphDatabase.newRelation<CreatorRelationship>(userId, eventNode)

            return eventNode
        } else {
            throw GroupAlreadyExists()
        }
    }

    fun editEvent(event: EventsDataEdit, eventId: Int): EventFullData? = transaction {
        val eventData = EventRow.findById(eventId)

        eventData?.apply {
            event.title?.let { title = it.trimAllSpaces() }
            event.description?.let { description = it.trimAllSpaces() }
            event.datetime?.let { datetime = it }
            event.duration?.let { duration = it }
            event.place?.let { place = it.trimAllSpaces() }
            event.link?.let { link = it.trimAllSpaces() }
        }?.let {
            EventFullData(
                it.id.value,
                it.title,
                it.authorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link
            )
        }
    }

    fun findEventsByParameters(parameters: EventDataSearch, offset: Int, amount: Int) = transaction {
        val query = EventsTable.selectAll()

        parameters.apply {
            ids?.let { ids ->
                query.andWhere { EventsTable.id inList ids }
            }

            title?.let { title ->
                query.andWhere { EventsTable.title like "%$title%" }
            }

            authorIds?.let { authorIds ->
                query.andWhere { EventsTable.authorId inList authorIds }
            }

            place?.let { place ->
                query.andWhere { EventsTable.place like "%$place%" }
            }

            datetimeMin?.let { datetimeMin ->
                query.andWhere { EventsTable.datetime greaterEq datetimeMin }
            }

            datetimeMax?.let { datetimeMax ->
                query.andWhere { EventsTable.datetime lessEq datetimeMax }
            }

            durationMin?.let { durationMin ->
                query.andWhere { EventsTable.duration greaterEq durationMin }
            }

            durationMax?.let { durationMax ->
                query.andWhere { EventsTable.duration lessEq durationMax }
            }

            description?.let { description ->
                query.andWhere { GroupsTable.description like "%$description%" }
            }
        }

        query.limit(amount, offset).map {
            EventFullData(
                it[EventsTable.id].value,
                it[EventsTable.title],
                it[EventsTable.authorId],
                it[EventsTable.place],
                it[EventsTable.datetime],
                it[EventsTable.duration],
                it[EventsTable.description],
                it[EventsTable.link]
            )
        }
    }
}
