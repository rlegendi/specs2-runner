package org.specs2

import org.specs2.reflect.Classes
import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure

object Specs2Bridge {
  def tryToCreateObject[T <: AnyRef](className: String, printMessage: Boolean = true, printStackTrace: Boolean = true,
    loader: ClassLoader = Thread.currentThread.getContextClassLoader)(implicit m: Manifest[T]): Option[T] = {

    Classes.tryToCreateObject(className, printMessage, printStackTrace, loader)(m)
  }
  
  def getContentFor(spec : SpecificationStructure) : Fragments = spec.content;
}
