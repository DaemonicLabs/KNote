dependencies {
    api(kotlin("stdlib", Constants.Kotlin.version))
    api(kotlin("stdlib-jdk8", Constants.Kotlin.version))

//    implementation(Constants.KotlinPoet.dependency)
    api(Constants.KotlinLogging.dependency)
    api(Constants.Html.dependency)

    api(
        group = "net.steppschuh.markdowngenerator",
        name = "markdowngenerator",
        version = "1.3.1.1"
    )
}