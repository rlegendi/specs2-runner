TODO
====

- Fix classpath entries, they are absolute path entries at the moment!
- Is the examples accessible in any binary build? That way we should'nt need duplicate example code in the tests.
- Handle Scope objects:

  // we need to extend Scope to be used as an Example body
  trait system extends Scope {
    val string = "Hey you"
  }
  case class system2() {
    val string = "Hey you"
    def e1 = string must have size(7)
  }
- Testing Spec2Finder could be done with `Spec1FinderSuite`, but probably there will be a better way to do that... hopefully (I'm not sure if I can link AST and Spec2 directly)

Things to read/watch
--------------------

* Esp. High Wizardry in the Land of Scala - sounds a great title :-)
  http://hacking-scala.posterous.com/scalaz-resources-for-beginners

Features I miss
---------------

- Unimplemented method declaration for Ctrl + Space
- Companion objects: show apply() signature parameters
- Correct Organize Imports


Scala stuff I found confusing first
-----------------------------------

- No type definition: Without the IDE, source is unreadable
- Everything is changing quickly, builds are volatile and shatter quickly
  (scala, sbt version-pending builds)
- Mixing 4 build tools (sbt/ivy/mvn/tychoo)

Incomplete features
-------------------

- That `"Fold Stack Trace"` buttion on the View: what should it do?
- Error reporting: e.g., "a" must be_==("b") doesn't write the output on the View

Specs2 typos
------------
Result.scala, line 288: "detailled"

Code Coverage
-------------

* Checked out Scct, http://mtkopone.github.com/scct/
  Unfortunately, it's quite Scala-Build tool dependant. E.g., the original repo works with only Sbt 0.10.1:

	https://github.com/dvc94ch/sbt-scct

  There's no support for 0.11.x, here's an issue about that:

	https://github.com/dvc94ch/sbt-scct/issues/7

  And it seems that there's no such movement on the project. This issue has been updated by different contributors:

	https://github.com/dvc94ch/sbt-scct/network

  I was able to use dimbleby's fork for the job:
	
	https://github.com/dimbleby/sbt-scct

  **Note** This is working only because he shared his personal Maven repo for the required artifacts:

	http://dimbleby.github.com/maven/ch/craven/

  And we needed this specific sbt version:

	http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt_2.9.1/

