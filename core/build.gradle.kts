dependencies {
    api(kotlin("stdlib", Kotlin.version))
    api(kotlin("stdlib-jdk8", Kotlin.version))
    api(kotlin("stdlib-jdk7", Kotlin.version))

    // script definition
    implementation(kotlin("scripting-jvm", Kotlin.version))

    // host
    implementation(kotlin("script-util", Kotlin.version))
    implementation(kotlin("scripting-jvm-host-embeddable", Kotlin.version))


    implementation(kotlin("compiler-embeddable", Kotlin.version))

    // not strictly necessary
    implementation(kotlin("reflect", Kotlin.version))

    implementation(KotlinPoet.dependency)
}