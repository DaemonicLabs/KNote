package knote.poet

import java.io.File

/**
 * This is just supposed to use for marking included pages
 */
@Deprecated("this was just supposed to be used by KotlinPoet to generate markers")
data class NotePage(
    val id: String,
    val file: File
)