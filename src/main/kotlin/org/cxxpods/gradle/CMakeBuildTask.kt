@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle

import org.cxxpods.gradle.util.PropFactory
import org.cxxpods.gradle.util.io
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

open class CMakeBuildTask : DefaultTask() {

  private val prop = PropFactory(this, project)

  /// region getters
//  @get:InputFile
//  var cmakeExe: File = File(project.cmakeExtension!!.cmakeExe)

//  @get:InputDirectory
//  var workingFolder = project.cmakeExtension!!.workingFolder

  //@get:Input
//  var buildType: CMakeBuildType? = null

  //  @get:Input
//  var target: String? = null
//
//  //@get:Input
//  var toolchain: CMakeToolchain? = null
//
  var config: CMakeTargetConfig? = null

  @get:Input
  @get:Optional
  var clean = false

  init {
    group = "cmake"
    description = "Build a configured Build with CMake"
  }

  fun configureFromProject() {
    val ext = project.extensions.getByName("cmake") as CMakePluginExtension

    clean = ext.clean
  }
  /// endregion

  private fun buildCmdLine(): CMakeCommandLine {
    val (_, target, toolchain, _, _, workingDir) = config!!
    return with(project) {
      CMakeCommandLine(this, workingDir).apply {
        arg("--build", ".")

        merge(cmakeExtension!!, toolchain, toolchain.buildOptions)

        if (target != "all") {
          io.mkdirs(File(workingDir, target))
          arg("--target", target)
        }
        if (clean)
          arg("--clean-first")

        merge(cmakeExtension!!, toolchain, toolchain.buildOptions)
        options.clear()

        makeToolOption("-j", cmakeExtension!!.parallel)

      }
    }
  }


  @TaskAction
  fun build() {
    CMakeExecutor(project, name)
      .exec(buildCmdLine())
  }

}
