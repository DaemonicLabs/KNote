package knote.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class KNoteDependencyScope(id: String, project: Project) {
    internal val knoteConfiguration: Configuration = project.configurations.create("knote_$id")
    internal val knoteFXConfiguration: Configuration = project.configurations.create("knoteFX_$id")
    internal val dependencies: MutableMap<Configuration, MutableList<String>> = mutableMapOf()

    fun knote(group: String, name: String, version: String) {
        val configuration = knoteConfiguration
        dependencies.getOrPut(configuration) { mutableListOf() } += "$group:$name:$version"
    }

    fun knotefx(group: String, name: String, version: String) {
        val configuration = knoteFXConfiguration
        dependencies.getOrPut(configuration) { mutableListOf() } += "$group:$name:$version"
    }
}
