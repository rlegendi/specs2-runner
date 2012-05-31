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
- Companion objects: show apply() signature


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

