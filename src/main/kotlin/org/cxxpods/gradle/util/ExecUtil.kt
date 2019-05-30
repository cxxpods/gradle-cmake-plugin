package org.cxxpods.gradle.util

import org.cxxpods.gradle.CMakePluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RunCommand(
  val project: Project,
  val parts: List<String>,
  val cwd: File
) {

  data class RunCommandResult(val stdout: String, val stderr: String?, val cmd: RunCommand)

  //private val running = AtomicBoolean(true)

  private val logger = project.logger


  private inner class StdStream : OutputStream() {

    val output = StringBuffer()

    override fun write(b: Int) {
      val s = b.toChar().toString()
      output.append(output)
      logger.quiet(s)
    }

    override fun write(b: ByteArray) {
      write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
      val s = String(b, off, len)
      output.append(s)
      logger.quiet(s)
    }
  }


  fun execute():RunCommandResult {
    val outWriter = StdStream()
    val errWriter = StdStream()
    try {
      project.exec {
        standardOutput = outWriter
        errorOutput = errWriter

        commandLine(parts)
        setWorkingDir(cwd)

      }.rethrowFailure()
    } finally {
      outWriter.close()
      errWriter.close()
    }

    return RunCommandResult(outWriter.toString(), errWriter.toString(), this)
  }

}


fun Project.cmd(
  parts: List<String>,
  cwd: File = extensions.getByType<CMakePluginExtension>().workingDir
) = RunCommand(this, parts, cwd).execute()