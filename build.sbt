organization  := "net.room271"

version       := "0.1.0"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.0"
  val sprayV = "1.3.1"
  val json4sV = "3.2.10"
  val elasticsearchV = "1.2.2"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test",
    "org.json4s"          %%  "json4s-native" % json4sV,
    "org.json4s"          %%  "json4s-ext"    % json4sV,
    "org.elasticsearch"   %  "elasticsearch" % elasticsearchV
  )
}

Revolver.settings
