@file:Suppress("MemberVisibilityCanBePrivate")

package org.cxxpods.gradle.util


import org.gradle.api.Project
import java.io.File

/**
 * Created by jglanz on 8/17/17.
 */
private val PATHS = listOf("/bin","/system/xbin","/usr/bin","/usr/local/bin")

/**
 * Exec Exception
 */
class ExecException(val exitCode:Int, val output:String): Exception("Exec failed: ${exitCode}")


private val projectIOMap = mutableMapOf<Project, ProjectIO>()

class ProjectIO(private val p: Project) : Project by p {

  /**
   * Exec a command simply returning the output
   */
  fun exec(vararg args:String):String = execWithFail(args.asList().toTypedArray(), false)

  /**
   * Exec and optionally throw error
   */
  fun execWithFail(args:Array<String>,throwErr:Boolean = true):String {
    val proc = Runtime.getRuntime().exec(args)
    val stdout = proc.inputStream.bufferedReader().use { it.readText() }

    val exitCode = proc.waitFor()
    if (exitCode != 0 && throwErr)
      throw ExecException(exitCode, stdout)
    return stdout
  }

  /**
   * Create directory and intermediate
   */
  fun rmdir(dir: File):Boolean {
    if (dir.exists() && dir.isDirectory) {
      val deletePath = dir.absolutePath

      val exitCode = Runtime.getRuntime().exec(
        arrayOf(
          which("rm"),
          "-Rf",
          deletePath
        ))
        .waitFor()

      if (exitCode != 0) {
        logger.info("Failed to rmdir: ${exitCode}")
        return false
      }
    }
    return true
  }

  /**
   * Get current working directory
   */
  val cwd:String
    get() = System.getProperty("user.dir")

  /**
   * Find program on search path
   */
  fun which(name:String, vararg paths: String, required: Boolean = false):String? {
    val allPaths = paths.toList() + PATHS
    allPaths
      .map { File(it,name) }
      .filter { it.exists() }
      .forEach { return it.absolutePath }

    if (required)
      throw AssertionError("Unable to find ${name} in ${allPaths.joinToString()}")

    return null
  }

  /**
   * Create directory and intermediate
   */
  fun mkdirs(dir: File):Boolean {
    if (!dir.exists() || !dir.isDirectory) {
      val exitCode = Runtime.getRuntime().exec(
        arrayOf(
          which("mkdir"),
          "-p",
          dir.absolutePath))
        .waitFor()

      if (exitCode != 0) {
        project.logger.info("Failed to mkdirs: ${exitCode}")
        return false
      }
    }
    return true
  }

  /**
   * Overload for string path
   */
  fun mkdirs(path:String):Boolean = mkdirs(File(path))
}

val Project.io
  get() = projectIOMap.getOrPut(this) {
    ProjectIO(this)
  }

fun filePath(vararg parts: String): File =
  File(parts.joinToString(File.separator))