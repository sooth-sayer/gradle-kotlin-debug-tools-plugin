# Tools plugin for debug tasks in gradle

## Install
1. git clone https://github.com/sooth-sayer/gradle-kotlin-debug-tools-plugin.git
2. ./gradlew build publishToMavenLocal
3. Go to your project directory
4. Add the following to your `build.gradle.kts` file in the `plugins` block:
```kotlin
id("debug-tools") version "1.0.0"
```
5. Add mavenLocal to your `settings.gradle.kts` file:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        ...
    }
}
```

## Usage
1. To debug your app (fun main have to be in the App.kt file in the org.example package)
```bash
./gradlew :app:debugWithJdb
```

2. To debug a test class
```bash
./gradlew --no-configuration-cache debugSingleTest -DtestClass=fully.qualified.ClassName
```

3. To debug a single test
```bash
./gradlew --no-configuration-cache debugSingleTest -DtestClass=fully.qualified.ClassName --tests fully.qualified.ClassName.testMethodName
```
