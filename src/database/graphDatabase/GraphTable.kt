import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Record
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

abstract class GraphTable(
    val tableName: String,
    val driver: Driver
) {
    inline fun <reified T : GraphNode> new(block: T.() -> Unit): T {
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
                        "${field.name}: null"
                }.filterNotNull().joinToString(", ")} }) RETURN (${clazzName.decapitalize()})"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseTo<T>()
            }.first()
        }
    }

    inline fun <reified T : GraphNode> find(block: T.() -> Unit): List<T> {
        val clazzConstructor = T::class.constructors.first()
        val clazz = clazzConstructor.call().apply {
            block()
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

            val returnQuery = clazzName.decapitalize()

            val matchQuery =
                "${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${params.joinToString(
                    ", "
                )} }"

            val query = "MATCH ($matchQuery) ${if (!where.isEmpty()) {
                "WHERE (${where.joinToString(" AND ")})"
            } else {
                ""
            }} RETURN ($returnQuery)"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseTo<T>()
            }
        }
    }

    inline fun <reified T : GraphNode> edit(nodeId: Int, block: T.() -> Unit): List<T> {
        val clazz = T::class.constructors.first().call().apply {
            block()
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
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
                "MATCH (${clazzName.decapitalize()}: ${clazzName.capitalize()}) WHERE (ID(${clazzName.decapitalize()}) = $nodeId) ${if (setQuery.isNotBlank()) "SET $setQuery" else ""} RETURN (${clazzName.decapitalize()})"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseTo<T>()
            }
        }
    }

    inline fun <reified T : GraphRelation> newRelation(firstNode: GraphNode, secondNode: GraphNode) {
        driver.session().readTransaction { transaction ->
            val query =
                "MATCH (a, b) WHERE ( ID(a) = ${firstNode.id} AND ID(b) = ${secondNode.id}) MERGE (a) - [$] -> (b)"
            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list {

            }
        }
    }

    inline fun <reified T : GraphNode> Record.parseTo(): T {
        val node = this[T::class.java.name.decapitalize()].asNode()

        return T::class.constructors.first().call().apply {
            this::class.memberProperties.forEach { property ->
                property as KMutableProperty<*>

                when (property.name) {
                    "id" -> {
                        property.setter.call(this, node.id().toInt())
                    }

                    else -> when (property.returnType.javaType) {
                        String::class.java ->
                            property.setter.call(this, node[property.name].asString())

                        Integer::class.java ->
                            property.setter.call(this, node[property.name].asInt())

                        Long::class.java ->
                            property.setter.call(this, node[property.name].asLong())

                        else -> {
                            println(property.returnType.javaType.typeName)
                            println(Long::class.java.typeName)
                        }
                    }
                }
            }
        }
    }
}