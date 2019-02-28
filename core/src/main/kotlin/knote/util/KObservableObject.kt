package knote.util

import kotlin.reflect.KProperty

interface KObservableObject<This, T> {
    val value: T
    val callbacks: MutableList<(newValue: T) -> Unit>
    operator fun getValue(self: This, property: KProperty<*>): T {
        return value
    }
}