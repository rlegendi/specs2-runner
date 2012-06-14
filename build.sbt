name := "specs2-runner"
 
version := "0.1.0"
 
scalaVersion := "2.9.1"

// If you use groupID %% artifactID % revision rather than groupID % artifactID % revision (the difference is the double %% after the groupID), sbt will add your project's Scala version to the artifact name. 
// https://github.com/harrah/xsbt/wiki/Getting-Started-Library-Dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-finders" % "1.0.1", 
  "org.scalatest" %% "scalatest"         % "2.0.M1", 
  "org.scalatest" %% "spec-runner"       % "0.2.0",
  // 1.11 is required because of Arguments visibility has been extended
  // 1.12 is required because it contains a fix of "embedded" specifications
  "org.specs2"    %% "specs2"            % "1.12-SNAPSHOT",
  "junit"         %  "junit"             % "4.10" % "test"
)

resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")

