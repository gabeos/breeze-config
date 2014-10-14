package breeze.config

/**
 * breeze-config
 * 10/6/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
/** The exception thrown in case something goes wrong in Configuration
  * @author dlwh
  */
abstract class ConfigurationException(msg: String) extends Exception(msg) {
  def param:String
}

/** The exception thrown in case something goes wrong in Configuration
  * @author dlwh
  */
class CannotParseException(param: String, msg: String) extends Exception(msg)

/**
 * The exception thrown for a missing property in Configuration
 */
class NoParameterException(msg: String, val param: String) extends ConfigurationException("while searching for " + param + ": " + msg)

/**
 * Exception thrown for unused properties
 */
class UnusedOptionsException[T:Manifest](val param: String, val unused: Set[String]) extends ConfigurationException(s"Some parameters were not read while parsing $param of type ${implicitly[Manifest[T]]}: $unused")
