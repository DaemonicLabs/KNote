package knote.util

interface KObservableMap<K, V> : Map<K, V> {
    val callbacks: MutableList<(oldValue: Map<K, V>, newValue: Map<K, V>) -> Unit>
}