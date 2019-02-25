package knote.util

interface KObservableMap<K, V> : Map<K, V> {
    val callbacks: MutableList<(newValue: Map<K, V>) -> Unit>
}