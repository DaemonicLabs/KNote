package knote.util

import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyMapWrapper
import javafx.collections.FXCollections
import tornadofx.*

inline val <reified E> KObservableList<E>.asObservable: ReadOnlyListWrapper<E>
    get() {
        val mutableList = FXCollections.observableArrayList<E>()
        val observableList = ReadOnlyListWrapper<E>(mutableList)
        callbacks += { new ->
            runLater {
                observableList.setAll(new)
            }
        }
        return observableList
    }
inline val <reified K, reified V> KObservableMap<K, V>.asObservable: ReadOnlyMapWrapper<K, V>
    get() {
        val mutableMap = FXCollections.observableHashMap<K, V>()
        val observableMap =  ReadOnlyMapWrapper<K, V>(mutableMap)
        callbacks += { new ->
            runLater {
                mutableMap.clear()
                mutableMap.putAll(new)
            }
        }
        return observableMap
    }