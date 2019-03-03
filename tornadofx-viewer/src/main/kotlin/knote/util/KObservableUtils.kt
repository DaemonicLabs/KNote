package knote.util

import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyMapProperty
import javafx.beans.property.ReadOnlyMapWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import knote.tornadofx.model.PageManagerChangeListener
import tornadofx.*

inline val <reified E> KObservableList<E>.asObservable: ReadOnlyListProperty<E>
    get() {
        val mutableList = FXCollections.observableArrayList<E>()
        val observableList = ReadOnlyListWrapper<E>(mutableList)
        callbacks += { new ->
            runLater {
                observableList.setAll(new)
            }
        }
        return observableList.readOnlyProperty
    }

inline val <reified K, reified V> KObservableMap<K, V>.asObservable: ReadOnlyMapProperty<K, V>
    get() {
        val mutableMap = FXCollections.observableHashMap<K, V>()
        val observableMap = ReadOnlyMapWrapper<K, V>(mutableMap)
        callbacks += { new ->
            runLater {
                val removed = mutableMap.entries - new.entries
                val added: Map<K, V> = (new.toMutableMap() - mutableMap) as Map<K, V>

                mutableMap.entries.removeAll(removed)
                mutableMap.putAll(added)
            }
        }
        return observableMap.readOnlyProperty
    }

inline val <reified This, reified T> KObservableObject<This, T>.asProperty: ReadOnlyObjectWrapper<T>
    get() {
        val wrapper = ReadOnlyObjectWrapper(value)
        callbacks += { new ->
            runLater {
                wrapper.set(new)
            }
        }

        return wrapper
    }
