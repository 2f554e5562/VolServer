import kotlin.reflect.KProperty

open class NotNullable<T : Any> {
    private var value: T? = null

    @Throws(IllegalStateException::class)
    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} not initialized.")
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
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
