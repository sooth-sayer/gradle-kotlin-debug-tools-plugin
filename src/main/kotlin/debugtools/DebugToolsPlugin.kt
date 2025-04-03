package debugtools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.testing.Test
import org.gradle.api.provider.Property
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import javax.inject.Inject
import org.gradle.process.ExecSpec


abstract class DebugWithJdbTask @Inject constructor(
  private val execOps: ExecOperations
) : DefaultTask() {

  @get:Input
  abstract val runtimeClasspath: Property<String>

  @TaskAction
  fun run() {
    println("✅ DebugWithJdbTask from published plugin is running!")

    execOps.exec {
      commandLine("osascript", "-e", """
        tell application "iTerm"
          create window with default profile
          tell current session of current window
            write text "jdb -sourcepath app/src/main/kotlin -attach localhost:5005"
          end tell
        end tell
      """.trimIndent())
    }

    execOps.exec {
      commandLine(
        "java",
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
        "-cp", runtimeClasspath.get(),
        "org.example.AppKt"
      )
    }
  }
}

class DebugToolsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.afterEvaluate {
      val hasKotlinJvm = project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")
      if (hasKotlinJvm) {
        val sourceSets = project.extensions
        .getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        val cp = sourceSets.getByName("main").runtimeClasspath.asPath

        project.tasks.register("debugWithJdb", DebugWithJdbTask::class.java).configure {
          group = "debug"
          description = "Run app with debugger"
          runtimeClasspath.set(cp)
        }
      } else {
        project.logger.warn("⚠️ Kotlin JVM plugin is not applied; skipping debugWithJdb setup.")
      }
    }

    project.tasks.register("debugSingleTest", Test::class.java).configure {
      // `this` — это и есть task типа Test
      useJUnitPlatform()

      val testClass = System.getProperty("testClass") ?: throw IllegalArgumentException("❗ Pass -DtestClass=fully.qualified.ClassName")

      val debugId = System.currentTimeMillis()

      jvmArgs = listOf(
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
        "-DdebugRunId=$debugId"
      )

      forkEvery = 1

      filter {
        includeTestsMatching(testClass)
      }

      testLogging {
        events("started", "passed", "skipped", "failed")
        showStandardStreams = true
      }

      doFirst {
        ProcessBuilder(
          "osascript", "-e", """
            tell application "iTerm"
              set newWindow to (create window with default profile)
              tell current session of current window
                write text "jdb -sourcepath app/src/main/kotlin:app/src/test/kotlin -attach localhost:5005"
              end tell
              set bounds of window 1 to {100, 100, 1000, 600} -- {left, top, right, bottom}
            end tell
          """.trimIndent()
        ).inheritIO() .start()
      }
    }
  }
}
