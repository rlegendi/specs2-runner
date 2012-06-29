package org.specs2

import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure
import org.specs2.reflect.Classes

object Specs2Bridge {
  def tryToCreateSpecification[T <: AnyRef](specs2Class: Class[_ <: SpecificationStructure]): SpecificationStructure = {
    // This is a private utility class that is inaccessible from outer packages
    Classes.tryToCreateObject(specs2Class.getName).get
  }

  // TODO Content is package-private, this is a workaround, consult with Eric
  // ERIC: that's intentional. It is to avoid the namespace of the Specification inheritor to be polluted with something he never uses
  // one way to make things nicer is to add "implicit" to your getContent definition. This way, any spec can be seen as the list
  // of Fragments it is holding
  implicit def getContentFor(spec: SpecificationStructure): Fragments = spec.content
}
