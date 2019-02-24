package knote.poet

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.File

interface PageMarker {
    abstract val id: String

    val fileName: String
        get() = "$id.page.kts"

    companion object {
        fun generate(output: File, pages: Array<out File>, fileName: String): List<File> {
            val filespec = FileSpec.builder(packageName = "", fileName = fileName).apply {
                val pages = TypeSpec.objectBuilder(fileName).apply {
                    pages.forEach { pageFile ->
                        val id = pageFile.name.substringBeforeLast(".page.kts")
//                        addProperty(
//                            PropertySpec.builder(id.capitalize(), NotePage::class)
//                                .initializer("%T(id = %S, file = %T(%S))", NotePage::class, id, File::class, pageFile.path)
//                                .build()
//                        )
                        addType(
                            TypeSpec.objectBuilder(id.capitalize())
                                .addSuperinterfaces(listOf(PageMarker::class.asClassName()))
                                .addProperty(
                                    PropertySpec.builder("id", String::class)
                                        .initializer("%S", id)
                                        .addModifiers(KModifier.OVERRIDE)
                                        .build()
                                )
                                .build()
                        )
                        Unit
                    }
                }.build()
                addType(pages)

            }.build()
            filespec.writeTo(output)

            return listOf(output.resolve("$fileName.kt"))
        }
    }
}