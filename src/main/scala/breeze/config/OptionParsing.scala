package breeze.config

import scala.reflect.Manifest
import scala.util.Try

/**
 * breeze-config
 * 10/6/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
trait OptionParsing {
  self: Configuration =>

  /**
   * Determines whether or not this type is an Option
   * @param man manifest to examine
   * @return
   */
  protected def isOptionType(man: Manifest[_]) = {
    classOf[Option[_]].isAssignableFrom(man.runtimeClass)
  }

  /**
   * Reads in an Option
   */
  protected def readOptTouched[T](prefix: String, contained: Manifest[T]): Try[(Option[T],Set[String])] = {
      readInTouched(prefix)(contained) map { case (tt,touched) => Some(tt) -> touched }
  }
}
