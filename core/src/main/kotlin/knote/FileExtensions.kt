package knote

import java.io.File
import kotlin.reflect.jvm.internal.impl.load.java.lazy.ContextKt.child



fun File.isSubDirectoryOf(folder: File): Boolean {
    val folder = folder.getCanonicalFile()
    val child = this.getCanonicalFile()

    var parentFile = child
    while (parentFile != null) {
        if (folder == parentFile) {
            return true
        }
        parentFile = parentFile.parentFile
    }
    return false
}