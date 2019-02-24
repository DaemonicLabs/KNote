plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    idea
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.0.1")
}


gradlePlugin {
    plugins {
        create("constGenerator") {
            id = "constantsGenerator"
            implementationClass = "plugin.GeneratorPlugin"
        }
    }
}