package knote.poet

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import java.io.File

data class NotePage(
    val file: File,
    val id: String
) {
    companion object {
        fun generate(output: File, pages: Array<out File>, fileName: String = "Pages"): List<File> {
            pages.map {

            }
            val filespec = FileSpec.builder(packageName = "", fileName = fileName).apply {
                pages.forEach { pageFile ->
                    val id = pageFile.name.substringBeforeLast(".page.kts")
                    addProperty(
                        PropertySpec.builder(id.capitalize(), NotePage::class)
                            .initializer("%T(id = %S, file = %T(%S))", NotePage::class, id, File::class, pageFile.path)
                            .build()
                    )
                }
            }.build()
            filespec.writeTo(output)

            return listOf(output.resolve("$fileName.kt"))
        }
    }
}