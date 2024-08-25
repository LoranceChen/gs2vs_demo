import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.archetypes.jar.LauncherJarPlugin
import sbt.Keys.*
import sbt.{Def, *}

object ScalacFlags {

  lazy val scalacFlagsFor2_13 = Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
//    "-Xfatal-warnings",
//    "-Ypatmat-exhaust-depth",
    "off",
    "-Xlint:_,-adapted-args,-package-object-classes,-stars-align,-unused,-byname-implicit",
    "-Ymacro-annotations"
  )

}

object CompilingSettings {

  type SettingType =
    Seq[Def.Setting[_ >: UpdateOptions with Boolean with Task[
      Seq[String]
    ] with String with Task[Seq[File]] with Seq[Resolver] with Seq[ModuleID]]]

  def createProject(projectName: String)(rootPath: String = "."): Project =
    Project(projectName, file(s"$rootPath"))

  // 公共配置
  lazy val CommonSettings: SettingType = Seq(
    organization := "gs2vs",
    scalaVersion := "2.13.14", // 子工程可根据需要覆盖
    Compile / scalacOptions ++= ScalacFlags.scalacFlagsFor2_13, // 子工程可根据需要覆盖
    Compile / doc / sources := Seq.empty,
    updateOptions := updateOptions.value.withLatestSnapshots(true),
    Compile / packageDoc / publishArtifact := false,
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-language:postfixOps",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
//      "-Xfatal-warnings",
//      "-Ypatmat-exhaust-depth",
      "off",
      "-Xlint:_,-adapted-args,-package-object-classes,-stars-align,-unused,-byname-implicit",
      "-Ymacro-annotations"
    )
  )

  implicit class SbtNativePackageSettings(project: Project) {
    def setJavaServerAppPackaging(compressClassPath: Boolean): Project = {
      val enablePackage = project.enablePlugins(JavaServerAppPackaging)
      val compressPackage =
        if (compressClassPath)
          enablePackage.enablePlugins(LauncherJarPlugin)
        else enablePackage

      compressPackage.settings(publish := {})
    }

  }

}
