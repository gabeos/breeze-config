package breeze.config

import scala.Predef.Manifest
import scala.reflect.Manifest

/**
 * breeze-config
 * 10/6/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
trait EitherParsing {
  self: Configuration =>

  /**
   * Determines whether or not this type is an Either
   * @param man manifest to examine
   * @return
   */
  protected def isEitherType(man: Manifest[_]) = {
    classOf[Either[_,_]].isAssignableFrom(man.runtimeClass)
  }

  protected def readEitherTouched[T,U](prefix: String, containedLeft: Manifest[T], containedRight: Manifest[U]): (Either[T,U],Set[String]) = {
    try {
      val (t,touched) = readInTouched(prefix)(containedLeft)
      (Left(t),touched)
    } catch {
      case
    }
  }

}
