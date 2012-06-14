package org.scalatest.specs2

import org.specs2.Specification
import org.specs2.specification.SpecificationStructure

/**
 * A set of constants and minimal utility functions.
 *
 * @author rlegendi
 */
object Utils {

  // --------------------------------------------------------------------------
  // --- Constants ------------------------------------------------------------

  /** Dedicated constant for <i>End of Line</i>. */
  val EoL = sys.props("line.separator")

  // --------------------------------------------------------------------------
  // --- Specs2 Utilities -----------------------------------------------------

  /**
   * Checks if the given specification has an assigned title.
   *
   * @param spec either an acceptance specification or a mutable (unit) specification;
   * 				<i>cannot be null</i>
   * @return <code>true</code> if the specification is assigned a title (e.g., with the <code>"...".title</code> notation);
   * 				<code>false</code> otherwise
   */
  def hasTitle(spec: SpecificationStructure): Boolean = {
    require(spec != null)

    // We exploit the representation here, should be covered with tests to catch if changes
    spec.identification.title != spec.identification.name
  }

  /**
   * Creates a nice suite name for the given specs2 specification instance.
   *
   * <p>
   * The suite name is used to display on the <i>ScalaTest View</i>.
   * </p>
   *
   * <p>
   * The specification title is more appropriate than the class name for a
   * <code>suiteName</code> because the user can specify a more readable name
   * for a specification, so it is accessed as a first try.
   * </p>
   *
   * @param spec either an acceptance specification or a mutable (unit) specification;
   * 				<i>cannot be null</i>
   * @return the <i>title</i> of the specification if it was set by the user;
   * 				<b>or</b> a bit decorated version of the name of the given
   * 				<code>spec</code> (if applicable)
   */
  def suiteNameFor(spec: SpecificationStructure): String = {
    require(spec != null)

    if (hasTitle(spec)) {
      return spec.identification.title
    } else {
      val baseName = spec.identification.name

      if (spec.isInstanceOf[Specification]) {
        return baseName + " Acceptance Specification"
      } else if (spec.isInstanceOf[org.specs2.mutable.Specification]) {
        return baseName + " Unit Specification"
      } else {
        // Nothing should fall into this category, but a default value is always good to have
        return baseName + " Specification"
      }
    }
  }

  /**
   * Returns a proper suite Id for the given specification instance that identifies it
   * during the execution of tests within the specification (it is used to fire/handle
   * different ScalaTest Events).
   *
   * @param spec either an acceptance specification or a mutable (unit) specification;
   * 				<i>cannot be null</i>
   * @return the Id used during the execution for ScalaTest for the given <code>spec</code>
   */
  def suiteIdFor(spec: SpecificationStructure): String = {
    require(spec != null)

    // TODO Ask Eric 2: Should I use fullName (i.e., decoded name) or javaClassName here?
    //                  fullName seems to have a bit more sense I guess
    spec.identification.fullName
  }
}
