plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("debugTools") {
            id = "debug-tools"
            implementationClass = "debugtools.DebugToolsPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}
