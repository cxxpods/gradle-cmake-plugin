package org.cxxpods.gradle.util

import org.cxxpods.gradle.CMakeOptions
import org.gradle.api.Project
import java.io.File

object CMakeDefaults {
  fun cmakeExe(project: Project, vararg paths: String) = with(project) {
    val exe = io.which("cmake", *paths, required = true)

    assert(File(exe).canExecute()) {
      "${exe} is not executable"
    }

    exe
  }



  fun defaults(project: Project, options: CMakeOptions) = with(project) {
    with(options) {
      cmakeExe = cmakeExe(project)
      sourceFolder = File(buildDir, "src${File.separator}main${File.separator}cpp")
    }
  }
}