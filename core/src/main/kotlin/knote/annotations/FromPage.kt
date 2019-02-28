package knote.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class FromPage(val source: String)