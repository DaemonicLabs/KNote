package knote.util

interface KObservableList<E> : List<E> {
    val callbacks: MutableList<(List<E>, List<E>) -> Unit>
}