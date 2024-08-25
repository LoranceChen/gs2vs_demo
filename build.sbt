import CompilingSettings._
import sbtprotobuf.{ProtobufPlugin => PB}

ThisBuild / version := "0.1.0-SNAPSHOT"

//ThisBuild / scalaVersion := "3.4.2"
ThisBuild / scalaVersion := "2.13.14"

lazy val apacheCommonsVersion = "2.6"
lazy val slf4jVersion = "2.0.13"
lazy val log4jVersion = "2.23.0" // fix for CVE-2021-45105
val scalaLoggingVersion = "3.9.2"
lazy val slf4jDependencies =
  Seq("org.slf4j" % "slf4j-api").map(_ % slf4jVersion)

lazy val log4jDependencies = Seq(
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-layout-template-json" % log4jVersion
)

/** -javaagent:"C:\personal\gs2vs\external\opentelemetry-javaagent_1.33.4.jar"
  * -Dotel.exporter.otlp.endpoint=http://xxx:xxx
  * -Dotel.exporter.otlp.protocol=grpc -Dotel.service.name=gs2vs
  * -Dotel.metrics.exporter=none
  */
// NOT EFFECT
// -Djdk.tracePinnedThreads=full
//ThisBuild / javaOptions ++= Seq(
//  """-javaagent:"C:\personal\gs2vs\external\opentelemetry-javaagent_1.33.4.jar"""",
//  "-Dotel.exporter.otlp.endpoint=http://xxx:xxx",
//  "-Dotel.exporter.otlp.protocol=grpc",
//  "-Dotel.service.name=gs2vs"
//)

lazy val PATH_GRPC_JAVA_PLUGIN = {
  System.getProperty("os.name").toLowerCase match {
    case mac if mac.contains("mac") =>
      val path = (project in file(
        "."
      )).base.toPath / "external/protoc-gen-grpc-java-1.66.0-osx-x86_64.exe"
      path
    case win if win.contains("win") =>
      val path = (project in file(
        "."
      )).base.toPath / "external/protoc-gen-grpc-java-1.58.0-windows-x86_64.exe"
      path
    case linux if linux.contains("linux") =>
      val path = (project in file(
        "."
      )).base.toPath / "external/protoc-gen-grpc-java-1.58.0-linux-x86_64.exe"
      path
    case osName =>
      throw new RuntimeException(s"Unknown operating system $osName")
  }
}

lazy val root =
  createProject("gs2vs")(".")
    .enablePlugins(PB)
    .settings(
      scalacOptions ++= Seq(
        "-Xlint:deprecation"
      ),
      //    ProtobufConfig / protobufProtocOptions ++= Seq("--plugin=EXECUTABLE"),
      //    ProtobufConfig / protobufProtoc := PATH_PROTOC,
      ProtobufConfig / protobufProtocOptions ++= Seq(
        "--plugin=protoc-gen-grpc-java=" + PATH_GRPC_JAVA_PLUGIN,
        "--grpc-java_out=" + baseDirectory.value + "/target/scala-2.13/src_managed/main/compiled_protobuf",
        "--proto_path=" + baseDirectory.value + "/proto_define"
      ),
      ProtobufConfig / sourceDirectories += (ProtobufConfig / protobufExternalIncludePath).value,
      ProtobufConfig / sourceDirectories += (new File("proto_define")),
      Compile / unmanagedResourceDirectories += (
        ProtobufConfig / sourceDirectory
      ).value
    )
    .settings(
      libraryDependencies ++= Seq(
        "com.lmax" % "disruptor" % "3.4.4",
        "io.netty" % "netty-all" % "4.1.112.Final",
        "com.zaxxer" % "HikariCP" % "5.1.0",
        "org.postgresql" % "postgresql" % "42.7.3",
        "com.google.inject" % "guice" % "7.0.0",
        "io.opentelemetry" % "opentelemetry-bom" % "1.40.0",
        "io.opentelemetry" % "opentelemetry-api" % "1.40.0",
        "io.opentelemetry" % "opentelemetry-sdk" % "1.40.0",
        "io.opentelemetry.semconv" % "opentelemetry-semconv" % "1.26.0-alpha",
        "io.opentelemetry" % "opentelemetry-exporter-logging" % "1.40.0",
        "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.40.0",
        "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.40.0",
        "org.msgpack" % "msgpack-core" % "0.9.8",
        "org.msgpack" % "jackson-dataformat-msgpack" % "0.9.8",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.2",
//        "com.google.protobuf" % "protobuf-java" % "3.25.4",
//        "com.google.protobuf" % "protobuf-java-util" % "3.25.4",
        "io.grpc" % "grpc-all" % "1.66.0",
        "javax.annotation" % "javax.annotation-api" % "1.3.2",
//        "com.google.protobuf" % "protobuf-java-util" % "3.25.4",
        "io.lettuce" % "lettuce-core" % "6.1.8.RELEASE",
        "org.apache.commons" % "commons-pool2" % "2.12.0",
        "io.pyroscope" % "agent" % "0.12.2"
      ) ++ log4jDependencies
    )
    .settings(Compile / sourceGenerators += Def.task {
      // adapt this for your build:
      val protoPackage = "io.grpc.examples.helloworld"

      val scalaFile =
        (Compile / sourceManaged).value / "_ONLY_FOR_INTELLIJ.scala"

      IO.write(
        scalaFile,
        s"""package $protoPackage
         |
         |private class _ONLY_FOR_INTELLIJ
         |""".stripMargin
      )

      Seq(scalaFile)
    }.taskValue)
