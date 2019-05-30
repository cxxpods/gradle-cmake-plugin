rootProject.name = "cmake-plugin"

pluginManagement {
  repositories {
    gradlePluginPortal()
    jcenter()
  }

  resolutionStrategy {
    eachPlugin {

      val module = when {
        requested.id.namespace == "com.jfrog" -> "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}"
        requested.id.namespace?.startsWith("org.jetbrains.kotlin") == true -> "org.jetbrains.kotlin:${Versions.kotlin}"
        else -> null
      }

      logger.info("Plugin requested (${requested.id.namespace}/${requested.id.name}): ${module}")
      if (module != null) {
        useModule(module)
      }

    }
  }
}