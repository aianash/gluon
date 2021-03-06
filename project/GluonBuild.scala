import sbt._
import sbt.Classpaths.publishTask
import Keys._

import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm, extraOptions, jvmOptions, scalatestOptions, multiNodeExecuteTests, multiNodeJavaName, multiNodeHostsFileName, multiNodeTargetDirName, multiTestOptions }

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

// import com.typesafe.sbt.SbtStartScript
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

import sbtassembly.AssemblyPlugin.autoImport._

import com.twitter.scrooge.ScroogeSBT

import org.apache.maven.artifact.handler.DefaultArtifactHandler

import com.typesafe.sbt.SbtNativePackager._, autoImport._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd, CmdLike}


object GluonBuild extends Build with Libraries {


  def sharedSettings = Seq(
    organization := "com.goshoplane",
    version := "0.0.1",
    scalaVersion := Version.scala,
    crossScalaVersions := Seq(Version.scala, "2.11.4"),
    scalacOptions := Seq("-unchecked", "-optimize", "-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions", "-language:postfixOps", "-language:reflectiveCalls", "-Yinline-warnings", "-encoding", "utf8"),
    retrieveManaged := true,

    fork := true,
    javaOptions += "-Xmx2500M",

    resolvers ++= Seq(
      // "ReaderDeck Releases" at "http://repo.readerdeck.com/artifactory/readerdeck-releases",
      "anormcypher" at "http://repo.anormcypher.org/",
      "Akka Repository" at "http://repo.akka.io/releases",
      "Spray Repository" at "http://repo.spray.io/",
      "twitter-repo" at "http://maven.twttr.com",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
    ),

    publishMavenStyle := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val gluon = Project(
    id = "gluon",
    base = file("."),
    settings = Project.defaultSettings
  ).aggregate(core, catalogue, service)


  lazy val core = Project(
    id = "gluon-core",
    base = file("core"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "gluon-core",

    libraryDependencies ++= Seq(
    ) ++ Libs.scalaz
      ++ Libs.scroogeCore
      ++ Libs.finagleThrift
      ++ Libs.libThrift
      ++ Libs.akka
      ++ Libs.scaldi
  )

  lazy val catalogue = Project(
    id = "gluon-catalogue",
    base = file("catalogue"),
    settings = Project.defaultSettings ++
      sharedSettings //++
      // SbtStartScript.startScriptForClassesSettings
  ).settings(
    name := "gluon-catalogue",

    libraryDependencies ++= Seq(
    ) ++ Libs.scalaz
      ++ Libs.scroogeCore
      ++ Libs.finagleThrift
      ++ Libs.libThrift
      ++ Libs.akka
      ++ Libs.slf4j
      ++ Libs.logback
      ++ Libs.finagleCore
      ++ Libs.mimepull
      ++ Libs.scaldi
      ++ Libs.scaldiAkka
      ++ Libs.bijection
      ++ Libs.kafkaClient
      ++ Libs.catalogueCommons
  ).dependsOn(core)

  lazy val service = Project(
    id = "gluon-service",
    base = file("service"),
    settings = Project.defaultSettings ++
      sharedSettings
      // SbtStartScript.startScriptForClassesSettings
  ).enablePlugins(JavaAppPackaging)
   .settings(
    name := "gluon-service",
    mainClass in Compile := Some("gluon.service.GluonServer"),

    dockerExposedPorts := Seq(2106),
    // TODO: remove echo statement once verified
    dockerEntrypoint := Seq("sh", "-c", "export GLUON_HOST=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1 }'` && echo $GLUON_HOST && bin/gluon-service $*"),
    dockerRepository := Some("docker"),
    dockerBaseImage := "phusion/baseimage",
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      new CmdLike {
        def makeContent = """|RUN \
                             |  echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
                             |  add-apt-repository -y ppa:webupd8team/java && \
                             |  apt-get update && \
                             |  apt-get install -y oracle-java7-installer && \
                             |  rm -rf /var/lib/apt/lists/* && \
                             |  rm -rf /var/cache/oracle-jdk7-installer""".stripMargin
      }
    ),
    libraryDependencies ++= Seq(
    ) ++ Libs.akka
      ++ Libs.slf4j
      ++ Libs.logback
      ++ Libs.finagleCore
      ++ Libs.mimepull
      ++ Libs.scaldi
      ++ Libs.scaldiAkka
      ++ Libs.bijection
      ++ Libs.play
      ++ Libs.scallop
      ++ Libs.scalajHttp
  ).dependsOn(core, catalogue)


}