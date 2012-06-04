package org.scalatest.specs2

import org.specs2.Specification
import org.specs2.specification.SpecificationStructure
import org.specs2.specification.SpecificationStructure

/**
 * A set of minimal utility functions.
 *
 * @author rlegendi
 */
object Utils {

  val EoL = sys.props("line.separator")

  /**
   * Creates a nice suite name for the given specs2 specification instance.
   *
   * <p>
   * The suite name is used to display on the <i>ScalaTest View</i>.
   * </p>
   *
   * @param spec either a mutable (unit) specification or a standard specification;
   * 				<i>cannot be null</i>
   * @return a decorated version of the given <code>spec</code> (if applicable)
   */
  // TODO Include .title (see UnitSpec example)
  def suiteNameFor(spec: SpecificationStructure): String = {
    require(spec != null)

    val baseName = spec.getClass.getSimpleName
    if (spec.isInstanceOf[Specification])
      return baseName + " specs2 Specification"
    else if (spec.isInstanceOf[org.specs2.mutable.Specification])
      return baseName + " specs2 Unit Specification"
    else
      // Nothing falls into this category jet, but a default value is always good to have
      return baseName + " specs2 Test"
  }

  //  // TODO Should I return Some(...) / None here?
  //  def suiteIdFor(spec: SpecificationStructure): String = {
  //    if (null == spec) throw new IllegalArgumentException("spec == null")
  //
  //    spec.getClass.getName
  //  }

  def suiteIdFor(spec: SpecificationStructure): String = {
    require(spec != null)
    spec.identification.fullName
  }
}
