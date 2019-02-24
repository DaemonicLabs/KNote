package knote.util

import com.sun.javafx.collections.ObservableListWrapper
import com.sun.javafx.collections.ObservableMapWrapper

inline val <reified E> KObservableList<E>.observable: ObservableListWrapper<E>
    get() {
        val observableList = ObservableListWrapper<E>(listOf())
        this.callbacks += { old,  new ->
            observableList.setAll(new)
        }
        return observableList
    }
inline val <reified K, reified V> KObservableMap<K, V>.observable: ObservableMapWrapper<K, V>
    get() {
        val observableMap =  ObservableMapWrapper<K, V>(mapOf())
        this.callbacks += { old,  new ->
            observableMap.clear()
            observableMap.putAll(new)
        }
        return observableMap
    }