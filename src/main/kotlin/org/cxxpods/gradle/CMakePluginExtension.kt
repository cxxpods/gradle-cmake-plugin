/**
 * Copyright 2019 Marco Freudenberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle

import org.cxxpods.gradle.util.CMakeDefaults
import org.cxxpods.gradle.util.PropFactory
import org.cxxpods.gradle.util.io
import org.gradle.api.Project
import java.io.File


open class CMakePluginExtension(private val project: Project) : CMakeOptions() {

  private val prop = PropFactory(this, project)

  // parameters used by config step
  var workingDir = File(project.buildDir,"cmake")

  var parallel by prop(Runtime.getRuntime().availableProcessors())

  var def = project.objects.mapProperty(String::class.java, String::class.java)

  val buildTypes = CMakeBuildTypeContainer(project).apply {
    create("Debug")
    create("Release")
  }
  val toolchains = CMakeToolchainContainer(project).apply {
    create("host")
  }

  val targets = mutableListOf("all")

  val targetConfigs by lazy {
    CMakeTargetConfigContainer(project,targets, toolchains, buildTypes)
  }

  val addToolchain = toolchains
  val addBuildType = buildTypes
  fun addTarget(target: String) = targets.add(target)

  var clean by prop(false)



  init {
    with(project) {
      CMakeDefaults.defaults(this, this@CMakePluginExtension)

    }
  }
  /// endregion getters





}