package knote.api

import kotlin.reflect.KProperty

interface DelegatedResult <This, T> {
    operator fun getValue(self: This, property: KProperty<*>): T
}