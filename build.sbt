name := "specs2-runner"
 
version := "0.1.0"
 
scalaVersion := "2.9.1"

// If you use groupID %% artifactID % revision rather than groupID % artifactID % revision (the difference is the double %% after the groupID), sbt will add your project's Scala version to the artifact name. 
// https://github.com/harrah/xsbt/wiki/Getting-Started-Library-Dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-finders" % "1.0.1", 
  "org.scalatest" %% "scalatest"         % "2.0.M1", 
  "org.specs2"    %% "specs2"            % "1.10",
  "org.specs2"    %% "spec-runner"       % "0.2.0"
)

resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"

