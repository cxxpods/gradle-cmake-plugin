@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle

open class CMakeInstallTask : CMakeAbstractTask() {


  init {
    group = "cmake"
    description = "Install target"
  }

  override fun buildCmdLine(): CMakeCommandLine {
    val (_, _, toolchain, _, _, workingDir) = config!!
    return with(project) {
      CMakeCommandLine(this, workingDir).apply {
        merge(cmakeExtension!!, toolchain, toolchain.buildOptions)
        options.clear()
        args.clear()

        arg("--build", ".", "--target", "install")

        makeToolOption("-j", cmakeExtension!!.parallel)
      }
    }
  }




}
