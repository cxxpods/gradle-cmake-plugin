package org.cxxpods.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import kotlin.properties.Delegates

abstract class CMakeAbstractTask : DefaultTask() {


  @get:Input
  var buildType: CMakeBuildType? = null
    protected set

  @get:Input
  var target: String? = null
    protected set

  @get:Input
  var toolchain: CMakeToolchain? = null
    protected set

  var config by Delegates.observable<CMakeTargetConfig?>(null) { prop, oldValue, newValue ->
    this.buildType = newValue?.buildType
    this.toolchain = newValue?.toolchain
    this.target = newValue?.target
  }

  protected abstract fun buildCmdLine(): CMakeCommandLine

  @TaskAction
  fun build() {
    CMakeExecutor(project, name)
      .exec(buildCmdLine())
  }
}