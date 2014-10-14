package breeze.config

import scala.util.Try

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

  protected def readEitherTouched[T,U](prefix: String, containedLeft: Manifest[T], containedRight: Manifest[U]): Try[(Either[T,U],Set[String])] = {
      readInTouched(prefix)(containedLeft).map({case (t,tt) => Left(t) -> tt}).orElse(readInTouched(prefix)(containedRight)).map({case (t,tt) => Right(t).asInstanceOf[Right[T,U]] -> tt})
  }

}
