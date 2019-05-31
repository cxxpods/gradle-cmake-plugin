package org.cxxpods.gradle
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.File

class CMakeExecutor internal constructor(private val project: Project, private val taskName: String) {

  private val logger = project.logger

  @Throws(GradleException::class)
  fun exec(cmdLine: CMakeCommandLine, cwd: File = cmdLine.workingDir) {
    val parts = cmdLine.build()
    logger.quiet("\tCMakePlugin.task $taskName - exec:")
    logger.quiet(parts.joinToString(" "))

    try {
      // make sure working folder exists
      cwd.mkdirs()

      with (project.logging) {
        val outLevel = standardOutputCaptureLevel
        val errorLevel = standardErrorCaptureLevel

        captureStandardError(LogLevel.QUIET)
        captureStandardOutput(LogLevel.QUIET)

        project.exec {
          setCommandLine(parts.first())
          args = parts.subList(1,parts.size)
          setWorkingDir(cwd)
        }.rethrowFailure()

        captureStandardError(errorLevel)
        captureStandardOutput(outLevel)
      }
    } catch (e: Throwable) {
      throw GradleScriptException("CMakeExecutor[$taskName].", e)
    }

  }


}

