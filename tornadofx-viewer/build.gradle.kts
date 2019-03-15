dependencies {
    api(project(":core"))
    api(Constants.TornadoFX.dependency)
    api(Constants.RichTextFX.dependency)

    implementation(kotlin("compiler-embeddable", Constants.Kotlin.version))
}