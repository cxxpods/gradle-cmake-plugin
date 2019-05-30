@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle

import org.cxxpods.gradle.util.PropFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import kotlin.properties.Delegates

open class CMakeConfigureTask : DefaultTask() {

  private val prop = PropFactory(this, project)

  /// region getters
//  @get:InputFile
//  @get:Optional
//  var cmakeExe: File? = null
//
//  @get:OutputDirectory
//  var workingFolder: File? = null
//
//  @get:InputDirectory
//  var sourceFolder: File? = null
//
//  @get:Input
//  @get:Optional
//  var installPrefix: String? = null

//@get:Input
//@get:Optional
//var generator by prop<String?>() // for example: "Visual Studio 16 2019"
//
//  @get:Input
//  @get:Optional
//  var platform by prop<String?>() // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0

//  @get:Input
//  @get:Optional
//  var toolset by prop<String?>() // for example "v142", supported on vs > 10.0

//  @get:Input
//  @get:Optional
//  var buildSharedLibs: Boolean? = null
//
//  @get:Input
//  @get:Optional
//  var buildStaticLibs: Boolean? = null
//
//  @get:Input
//  @get:Optional
//  var def = project.objects.mapProperty(String::class.java, String::class.java)

  //@get:Input
//  var buildType by prop<CMakeBuildType?>()

//  @get:InputFile
//  @get:Optional
//  var toolchainFile by prop<File?>()

  //@get:Input
//  var toolchain by Delegates.observable<CMakeToolchain?>(null) { _, _, newValue ->
//    val newToolchainFile = newValue?.toolchainFile
//    if (newToolchainFile == null) {
//      toolchainFile = null
//    } else {
//      assert(newToolchainFile.exists()) {
//        logger.error("Toolchain file does not exist: ${newToolchainFile.absolutePath}")
//      }
//
//      toolchainFile = newToolchainFile
//    }
//  }
  var config: CMakeTargetConfig? = null
  init {
    group = "cmake"
    description = "Configure a Build with CMake"

    // default values


  }

//  fun configureFromProject() {
//    val ext = project.extensions.getByName("cmake") as CMakePluginExtension
//    executable = ext.executable
//    workingFolder = ext.workingFolder
//    sourceFolder = ext.sourceFolder
//    configurationTypes = ext.configurationTypes
//    installPrefix = ext.installPrefix
//    generator = ext.generator
//    platform = ext.platform
//    toolset = ext.toolset
//    buildSharedLibs = ext.buildSharedLibs
//    buildStaticLibs = ext.buildStaticLibs
//    def = ext.def
//  }
  /// endregion

  private fun buildCmdLine(): CMakeCommandLine {
    val (_, _, toolchain, buildType, _, workingDir) = config!!
    val toolchainFile = toolchain.toolchainFile
    return with(project) {
      CMakeCommandLine(this, workingDir).apply {
        merge(cmakeExtension!!,toolchain, toolchain.configOptions)

        arg("-G", generator)
//      arg("-A", platform)
//      arg("-T", toolset)

        option("CMAKE_TOOLCHAIN_FILE", toolchainFile?.absolutePath)
        option("CMAKE_BUILD_TYPE", buildType.name)
        option("CMAKE_INSTALL_PREFIX", installPrefix)


        if (buildSharedLibs != null)
          option("BUILD_SHARED_LIBS", buildSharedLibs.toString())

        if (buildStaticLibs != null)
          option("BUILD_STATIC_LIBS", buildStaticLibs.toString())


//        if (def.isPresent) {
//          def.get().entries.forEach { (key, value) ->
//            option(key, value)
//          }
//        }



        if (sourceFolder != null)
          positional(sourceFolder!!.toRelativeString(workingDir))


      }
    }
  }

  @TaskAction
  fun configure() {
    CMakeExecutor(project, name)
      .exec(buildCmdLine())
  }

}
