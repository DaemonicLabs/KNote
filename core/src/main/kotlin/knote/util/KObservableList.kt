package knote.util

interface KObservableList<E> : List<E> {
    val callbacks: MutableList<(List<E>) -> Unit>
}