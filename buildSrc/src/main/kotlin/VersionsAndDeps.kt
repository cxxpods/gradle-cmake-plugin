
object Versions {
  val junit = "5.4.2"
  val bintray = "1.8.4"
  val kotlin = "1.3.21"
  val pluginPublish = "0.10.1"
}

object Deps {
  val bintray = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}"

  val junitApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
  val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"


}