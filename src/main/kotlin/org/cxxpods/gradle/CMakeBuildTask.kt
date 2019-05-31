@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle

import org.cxxpods.gradle.util.io
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.File

open class CMakeBuildTask : CMakeAbstractTask() {

  @get:Input
  @get:Optional
  var clean = false

  init {
    group = "cmake"
    description = "Build a configured Build with CMake"
  }

  override fun buildCmdLine(): CMakeCommandLine {
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

        options.clear()

        makeToolOption("-j", cmakeExtension!!.parallel)

      }
    }
  }




}
