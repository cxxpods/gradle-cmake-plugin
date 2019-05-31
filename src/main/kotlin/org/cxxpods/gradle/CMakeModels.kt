package org.cxxpods.gradle

import org.cxxpods.gradle.util.CMakeDefaults
import org.cxxpods.gradle.util.env
import org.cxxpods.gradle.util.io
import org.gradle.api.Project
import java.io.File
import java.util.*
import kotlin.collections.LinkedHashSet

class CMakeToolchainContainer(private val project: Project) : MutableList<CMakeToolchain> by mutableListOf() {

  fun create(name: String, toolchainFile: File? = null, configure: (CMakeToolchain.() -> Unit) = { }): CMakeToolchain {
    val chain = CMakeToolchain(project, name, toolchainFile)
    chain.configure()
    add(chain)
    return chain
  }

  operator fun invoke(name: String, toolchainFile: File? = null, configure: (CMakeToolchain.() -> Unit) = { }): CMakeToolchain {
    return create(name, toolchainFile, configure)
  }
}

data class CMakeBuildType(val name: String)

class CMakeBuildTypeContainer(project: Project) : CMakeOptions(), MutableSet<CMakeBuildType> by LinkedHashSet() {

  fun create(name: String): CMakeBuildType {
    val type = CMakeBuildType(name)
    add(type)
    return type
  }

  operator fun invoke(name: String) = create(name)
}

data class CMakeToolchain(
  private val project: Project,
  val name: String,
  val toolchainFile: File? = null,
  val configOptions: CMakeOptions = CMakeOptions(),
  val buildOptions: CMakeOptions = CMakeOptions()
) : CMakeOptions()

class CMakeTargetConfigContainer(
  private val project: Project,
  val targets: List<String>,
  val toolchains: CMakeToolchainContainer,
  val buildTypes: CMakeBuildTypeContainer
) : List<CMakeTargetConfig> by listOf(
  *targets
    .map { target ->
      toolchains
        .map { chain ->
          buildTypes
            .map { type ->
              CMakeTargetConfig(project, target, chain, type)
            }
        }
        .flatten()
    }
    .flatten()
    .toTypedArray()
) {

  fun create(target: String, toolchain: CMakeToolchain, buildType: CMakeBuildType): CMakeTargetConfig {
    return CMakeTargetConfig(project, target, toolchain, buildType)
  }

  operator fun invoke(target: String, toolchain: CMakeToolchain, buildType: CMakeBuildType) = create(target, toolchain, buildType)


}

fun String.toTitleCase() = if (isNotBlank()) this[0].toUpperCase() + substring(1)
else this

fun String.toLowerTitleCase() = if (isNotBlank()) this[0].toLowerCase() + substring(1)
else this

data class CMakeTargetConfig(
  val project: Project,
  val target: String,
  val toolchain: CMakeToolchain,
  val buildType: CMakeBuildType,
  val name: String = toName(null, target, buildType, toolchain).toLowerTitleCase(),
  val workingDir: File = File(project.cmakeExtension!!.workingDir, "${name}/${target}"),
  val configTaskName: String = toTaskName("configure", target, buildType, toolchain),
  val buildTaskName: String = toTaskName("build", target, buildType, toolchain),
  val installTaskName: String = toTaskName("install", target, buildType, toolchain),
  val cleanTaskName: String = toTaskName("clean", target, buildType, toolchain)
) {


  companion object {

    fun toName(action: String?, target: String, buildType: CMakeBuildType, toolchain: CMakeToolchain) =
      listOfNotNull(action, target, buildType.name, toolchain.name)
        .joinToString("") { it.toTitleCase() }

    fun toTaskName(action: String, target: String, buildType: CMakeBuildType, toolchain: CMakeToolchain) =
      "cmake" + toName(action, target, buildType, toolchain)

  }

  init {
    project.io.mkdirs(workingDir)

  }

}


open class CMakeOptions {

  var cmakeExe: String? = null

  var sourceFolder: File? = null

  var installPrefix: String? = null

  var generator: String? = null

  //  @get:Input
//  @get:Optional
  var buildSharedLibs: Boolean? = null
//
//  @get:Input
//  @get:Optional
  var buildStaticLibs: Boolean? = null

  val args = LinkedHashSet<String>()

  val options = mutableMapOf<String, String>()

  fun arg(vararg newArgs: String?) = run {
    if (newArgs.all { it != null })
      args.addAll(listOf(*newArgs
        .filterNotNull().filter { param ->
          param.isNotEmpty()
        }.toTypedArray()))

  }

  fun option(name: String, value: Any?) {
    if (value != null)
      options[name] = value.toString()


  }



  fun toArgs() = args.toTypedArray()
  fun toOptions() = options.entries.map { (name, value) -> "-D${name}=${value}" }.toTypedArray()

  fun merge(vararg others: CMakeOptions): CMakeOptions {
    others.forEach { other ->
      if (other.cmakeExe != null)
        cmakeExe = other.cmakeExe

      if (other.sourceFolder != null)
        sourceFolder = other.sourceFolder
      if (other.installPrefix != null)
        installPrefix = other.installPrefix
      if (other.generator != null)
        generator = other.generator

      if (other.buildSharedLibs != null)
        buildSharedLibs = other.buildSharedLibs

      if (other.buildStaticLibs != null)
        buildStaticLibs = other.buildStaticLibs

      args.addAll(other.args)
      options.putAll(other.options)
    }
    return this
  }


  fun useAndroidNDK(
    sdkHome: String = env("ANDROID_SDK", "ANDROID_HOME") ?: error("ANDROID_SDK/ANDROID_HOME not available in env variables"),
    ndkHome: String? = env("ANDROID_NDK") ?: run {
      val dir = File(sdkHome, "ndk-bundle")
      assert(dir.exists()) { "Unable to determine ANDROID_NDK from env or by check the SDK ${sdkHome}"}
      dir.absolutePath
    }
  ) {
    val sdk = File(sdkHome)
    val ndk = File(ndkHome)
    val sysroot = File(ndk, "sysroot")

    arrayOf(sdk,ndk,sysroot)
      .forEach { assert(it.exists()) }

    val cmakeExes = sdk.walk()
      .maxDepth(4)
      .filter { it.nameWithoutExtension == "cmake" && it.canExecute() && !it.isDirectory }
      .toList()

    assert(cmakeExes.isNotEmpty()) { "Unable to find any `cmake` versions installed"}

    cmakeExe = cmakeExes.first().absolutePath
    option("ANDROID_SDK", sdk.absolutePath)
    option("ANDROID_NDK", ndk.absolutePath)
  }

  operator fun invoke(configure: CMakeOptions.() -> Unit) = this.configure()

  val opt = ::option

  val define = ::option
}

val Project.cmakeExtension
  get() = try {
    extensions.getByType(CMakePluginExtension::class.java)
  } catch (ex: Exception) {
    null
  }

open class CMakeCommandLine(project: Project, val workingDir: File) : CMakeOptions() {

  private val positionals = mutableListOf<String>()

  private val makeToolOptions = mutableListOf<String>()

  fun positional(vararg values: Any?) {
    values.filterNotNull().forEach {
      positionals.add(it.toString())
    }
  }

  fun makeToolOption(vararg values: Any?) {
    values.mapNotNull { it?.toString() }.forEach {
      if (makeToolOptions.isEmpty())
        makeToolOptions.add("--")

      makeToolOptions.add(it)
    }
  }

  fun build() =
    listOf(
      cmakeExe,
      *toArgs(),
      *toOptions(),
      *positionals.toTypedArray(),
      *makeToolOptions.toTypedArray()
    )


}