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

