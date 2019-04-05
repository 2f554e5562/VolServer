import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class NotNullable<T : Any> {
    private var value: T? = null

    var observableRelation: String? = null

    @Throws(IllegalStateException::class)
    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} not initialized.")
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    inline fun <reified R : GraphRelationship> observeRelation(): NotNullable<T> {
        observableRelation = R::class.java.name

        return this
    }
}

open class Nullable<T : Any?> {
    private var value: T? = null

    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
    }
}
