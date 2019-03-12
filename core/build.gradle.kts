dependencies {
    api(kotlin("stdlib", Constants.Kotlin.version))
    api(kotlin("stdlib-jdk8", Constants.Kotlin.version))
    api(kotlin("stdlib-jdk7", Constants.Kotlin.version))

    // script definition
    implementation(kotlin("scripting-jvm", Constants.Kotlin.version))

    // host
    implementation(kotlin("script-util", Constants.Kotlin.version))
    implementation(kotlin("scripting-jvm-host-embeddable", Constants.Kotlin.version))


    implementation(kotlin("compiler-embeddable", Constants.Kotlin.version))

    // not strictly necessary
    implementation(kotlin("reflect", Constants.Kotlin.version))

    implementation(Constants.KotlinPoet.dependency)
    api(Constants.KotlinLogging.dependency)
    implementation(Constants.LogbackClassic.dependency) {
        exclude(group = "com.com.mail", module = "javax.mail")
    }

    api(Constants.Coroutines.dependency)
    api(project(":markdown"))
}