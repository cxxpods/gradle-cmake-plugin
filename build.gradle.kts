@file:Suppress("UnstableApiUsage")

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.publish.PublicationContainer
import java.util.Date

buildscript {
  repositories {
    jcenter()
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  id("com.gradle.plugin-publish") version Versions.pluginPublish
  id("java-library")
  id("com.jfrog.bintray")

}

val pluginId =properties["PLUGIN_ID"] as String
group = properties["GROUP"] as String
version = properties["VERSION"] as String

gradlePlugin {
  plugins {
    create(pluginId) {
      id = "${project.group}.${project.name}"
      implementationClass = "org.cxxpods.gradle.CMakePlugin"
    }
  }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
  archiveClassifier.set("sources")
  from(kotlin.sourceSets["main"].kotlin.srcDirs)
}


/**
 * Artifacts (SOURCES)
 */
artifacts {
  add("archives", sourcesJar)
}

pluginBundle {

  website = "https://github.com/cxxpods/gradle-cmake-plugin"
  vcsUrl = "https://github.com/cxxpods/gradle-cmake-plugin"

  plugins {
    getByName(pluginId) {
      displayName = "Kotlin Object (KO) Generator"
      description = "Properties converted to Kotlin Objects"
      tags = listOf("kotlin","cmake","c++","c","variant")
      version = project.version as String
    }
  }

  mavenCoordinates {
    groupId = project.group as String
    artifactId = project.name
    version = project.version as String
  }
}

lateinit var allPublications: PublicationContainer
configure<PublishingExtension> {
  repositories {
    val repoDir = File(buildDir, "repository")
    repoDir.mkdirs()

    mavenLocal()
    maven(url = repoDir.absolutePath)
  }

  allPublications = publications

  publications.withType(MavenPublication::class.java) {
    groupId = project.group as String
    artifactId = project.name
    version = project.version as String

    artifact(sourcesJar.get())
  }
}


repositories {
  jcenter()
  mavenCentral()
  google()
  gradlePluginPortal()
}


dependencies {
  compileOnly(gradleApi())
  testImplementation(Deps.junitApi)
  testRuntimeOnly(Deps.junitEngine)
}


tasks.withType<Test> {
  useJUnitPlatform()
}

afterEvaluate {
  configure<BintrayExtension> {
    user = "jonglanz"
    key = System.getenv("BINTRAY_API_KEY") ?: ""
    publish = true
    override = true
    setPublications(*allPublications.map { it.name }.toTypedArray())
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
      repo = "oss"
      name = project.name
      userOrg = "densebrain"
      setLicenses("MIT")
      vcsUrl = "https://github.com/cxxpods/gradle-cmake-plugin.git"
      setVersion(VersionConfig().apply {
        released = Date().toString()
        name = project.version as String
      })
    })
  }

  tasks.getByName("publish").dependsOn("bintrayUpload", "assemble")
}