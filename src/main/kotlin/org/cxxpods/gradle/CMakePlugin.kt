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

package org.cxxpods.gradle

import org.cxxpods.gradle.util.cmd
import org.cxxpods.gradle.util.io
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

open class CMakePlugin : Plugin<Project> {
  private fun deleteDirectory(directoryToBeDeleted: File): Boolean {
    val allContents = directoryToBeDeleted.listFiles()
    if (allContents != null) {
      for (file in allContents) {
        deleteDirectory(file)
      }
    }
    return directoryToBeDeleted.delete()
  }

  override fun apply(project: Project) {
    val e = project.extensions.create("cmake", CMakePluginExtension::class.java, project)

    /*
         * cmakeConfigureTask
         */
    project.afterEvaluate {
      project.apply {
        tasks.apply {

          val (configTasks, buildTasks, installTasks, cleanTasks) =
            e.targetConfigs.fold(
              listOf<MutableList<Task>>(
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
              )
            ) { (configTasks, buildTasks, installTasks, cleanTasks), t ->

              io.mkdirs(t.workingDir)
              val workingTargetDir = File(t.workingDir, t.target).apply {
                mkdirs()
              }
              //val (_, _, _, _, configTaskName, buildTaskName, cleanTaskName) = t
              val configTask = create(t.configTaskName, CMakeConfigureTask::class.java) {
                group = "cmake"
                description = "Configure CMake Target"
                config = t
              }

              /**
               * Make build task
               */
              val buildTask = create(t.buildTaskName, CMakeBuildTask::class.java) {
                dependsOn(configTask)

                group = "cmake"
                description = "Build CMake Target"
                config = t

              }

              /**
               * Make install task
               */
              val installTask = create(t.installTaskName, CMakeInstallTask::class.java) {
                dependsOn(buildTask)

                group = "cmake"
                description = "Install CMake Target"
                config = t

              }


              val cmakeClean = create(t.cleanTaskName) {
                group = "cmake"
                description = "Clean CMake Target"
                doFirst {
                  // should go to clean...
                  val workingFolder = workingTargetDir.absoluteFile

                  if (workingFolder.exists()) {
                    logger.info("Deleting folder $workingFolder")
                    if (!deleteDirectory(workingFolder))
                      throw GradleException("Could not delete working folder $workingFolder")
                  }
                }

              }

              configTasks.add(configTask)
              buildTasks.add(buildTask)
              cleanTasks.add(cmakeClean)
              installTasks.add(installTask)

              listOf(configTasks, buildTasks, installTasks, cleanTasks)
            }

          val cmakeGenerators = create("cmakeGenerators") {
            doFirst {
              // should go to clean...
              val result = cmd(listOf(e.cmakeExe!!, "--help"))
              val lines = result.stdout.split("\n")
              val index = lines.indexOf("Generators")
              if (index > -1) {
                val generators = lines.subList(index + 1, lines.size)
                logger.quiet("Generators:\n\t${generators.joinToString("\n\t")}")
              }
            }
          }
          cmakeGenerators.group = "cmake"
          cmakeGenerators.description = "List available CMake generators"


          create("cmakeConfigureAll") {
            dependsOn(configTasks)
          }

          create("cmakeBuildAll") {
            dependsOn(buildTasks)
          }

          create("cmakeCleanAll") {
            dependsOn(cleanTasks)
          }

          create("cmakeInstallAll") {
            dependsOn(installTasks)
          }
        }
      }
    }
  }
}