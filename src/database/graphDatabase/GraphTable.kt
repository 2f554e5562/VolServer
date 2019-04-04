import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Record
import java.lang.IllegalStateException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

abstract class GraphTable(
    val tableName: String,
    val driver: Driver
) {
    init {
        driver.session().run("MATCH (n) WHERE (ID(n) = -1) RETURN n;")
        println("Database ${this::class.java.name} - CONNECTED")
    }

    inline fun <reified T : GraphNode> newNode(block: T.() -> Unit): T {
        val clazz = T::class.constructors.first().call().apply {
            block()
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val query =
                "CREATE (${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${clazz::class.memberProperties.map { field ->
                    field.isAccessible = true

                    val fieldValue = try {
                        field.getter.call(clazz)
                    } catch (e: InvocationTargetException) {
                        return@map null
                    }

                    return@map if (fieldValue != null)
                        when (field.returnType.javaType) {
                            String::class.java -> {
                                "${field.name}: \"$fieldValue\""
                            }

                            else -> "${field.name}: $fieldValue"
                        }
                    else
                        null
                }.filterNotNull().joinToString(", ")} }) RETURN (${clazzName.decapitalize()})"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }.first()
        }
    }

    inline fun <reified T : GraphNode> findNode(limit: Long = 20, offset: Long = 0, block: T.(Filter) -> Unit): List<T> {
        val filter = Filter(T::class.java)

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val params = mutableListOf<String>()
            val where = mutableListOf<String>()

            clazz::class.memberProperties.forEach { field ->
                field.isAccessible = true

                val fieldValue = try {
                    field.getter.call(clazz)
                } catch (e: InvocationTargetException) {
                    return@forEach
                }

                when (field.name) {
                    "id" -> {
                        where.add("ID(${clazzName.decapitalize()}) = $fieldValue")
                    }
                    else -> {
                        if (fieldValue != null)
                            params.add("${field.name}: \"$fieldValue\"")
                    }
                }
            }

            where.addAll(filter.filters)

            val returnQuery = clazzName.decapitalize()

            val matchQuery =
                "${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${params.joinToString(
                    ", "
                )} }"

            val query = "MATCH ($matchQuery) ${if (!where.isEmpty()) {
                "WHERE (${where.joinToString(" AND ")})"
            } else {
                ""
            }} RETURN ($returnQuery) SKIP $offset LIMIT $limit"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified T : GraphNode> editNode(nodeId: Long, block: T.(Filter) -> Unit): List<T> {
        val filter = Filter(T::class.java)

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val where = mutableListOf<String>()

            where.add("ID(${clazzName.decapitalize()}) = $nodeId")

            where.addAll(filter.filters)

            val setQuery = clazz::class.memberProperties.map { field ->
                field.isAccessible = true

                try {
                    val fieldValue = field.getter.call(clazz)

                    return@map if (fieldValue != null)
                        when (field.returnType.javaType) {
                            String::class.java -> {
                                "${clazzName.decapitalize()}.${field.name} = \"$fieldValue\""
                            }

                            else -> "${clazzName.decapitalize()}.${field.name} = $fieldValue"
                        }
                    else
                        null
                } catch (e: InvocationTargetException) {
                    return@map null
                }
            }.filterNotNull().joinToString(", ")

            val query =
                "MATCH (${clazzName.decapitalize()}: ${clazzName.capitalize()}) WHERE (${if (where.isNotEmpty()) where.joinToString(" AND ") else ""}) ${if (setQuery.isNotBlank()) "SET $setQuery" else ""} RETURN (${clazzName.decapitalize()})"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified T : GraphRelationship> newRelationship(firstNodeId: Long, secondNodeId: Long): GraphRelationship {
        return driver.session().readTransaction { transaction ->
            val clazz = T::class.constructors.first().call()

            val clazzName = clazz::class.java.name

            val query = "MATCH (a), (b) WHERE ( ID(a) = $firstNodeId AND ID(b) = $secondNodeId) MERGE (a) - [${clazzName.decapitalize()}: ${clazzName.capitalize()}] -> (b) RETURN (${clazzName.decapitalize()});"
            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToRelationship<T>()
            }.first()
        }
    }

    inline fun <reified T : GraphNode, reified R : CreatorRelationship> findRelationshipNode(nodeId: Long, limit: Long = 20, offset: Long = 0, block: T.(Filter) -> Unit): List<T> {
        val filter = Filter(T::class.java)

        val relationshipName = R::class.java.name

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val params = mutableListOf<String>()
            val where = mutableListOf<String>()

            clazz::class.memberProperties.forEach { field ->
                field.isAccessible = true

                val fieldValue = try {
                    field.getter.call(clazz)
                } catch (e: InvocationTargetException) {
                    return@forEach
                }

                when (field.name) {
                    "id" -> {
                        where.add("ID(${clazzName.decapitalize()}) = $fieldValue")
                    }
                    else -> {
                        if (fieldValue != null)
                            params.add("${field.name}: \"$fieldValue\"")
                    }
                }
            }

            where.addAll(filter.filters)

            where.add("ID(a) = $nodeId")

            val matchQuery = "(a) - [${relationshipName.decapitalize()}: ${relationshipName.capitalize()}] -> (${clazzName.decapitalize()}: ${clazzName.capitalize()})"

            val query = "MATCH $matchQuery WHERE (${if (!where.isEmpty()) {
                "WHERE (${where.joinToString(" AND ")})"
            } else {
                ""
            }}) RETURN (${clazzName.decapitalize()}) SKIP $offset LIMIT $limit"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified T : GraphNode> Record.parseToNode(): T {
        val node = this[T::class.java.name.decapitalize()].asNode()

        return T::class.constructors.first().call().apply {
            this::class.memberProperties.forEach { property ->
                property as KMutableProperty<*>

                when (property.name) {
                    "id" -> {
                        property.setter.call(this, node.id().toInt())
                    }

                    else -> {
                        if (node[property.name].isNull) return@forEach

                        when (property.returnType.javaType) {
                            String::class.java ->
                                property.setter.call(this, node[property.name].asString())

                            Integer::class.java ->
                                property.setter.call(this, node[property.name].asInt())

                            Long::class.java ->
                                property.setter.call(this, node[property.name].asLong())

                            else -> throw IllegalStateException("Illegal parse type.")
                        }
                    }
                }
            }
        }
    }

    inline fun <reified T : GraphRelationship> Record.parseToRelationship(): T {
        val relationship = this[T::class.java.name.decapitalize()].asRelationship()

        return T::class.constructors.first().call().apply {
            this::class.memberProperties.forEach { property ->
                property as KMutableProperty<*>

                id = relationship.id()
                startNodeId = relationship.startNodeId()
                endNodeId = relationship.endNodeId()
            }
        }
    }
}