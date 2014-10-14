package breeze.config

import scala.collection.generic.{CanBuildFrom, GenericCompanion}
import scala.collection.mutable
import scala.reflect.Manifest
import scala.util.{Success, Failure, Try}

/**
 * breeze-config
 * 10/6/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
trait CollectionParsing {
  self: Configuration =>
  /**
   * Determines whether or not this type is a collection type
   * @param man the manifest to examine
   * @return
   */
  protected def isCollectionType(man: Manifest[_]) = {
    man.runtimeClass.isArray || classOf[Iterable[_]].isAssignableFrom(man.runtimeClass)
  }


  /**
   * Reads in a sequence by looking for properties of the form prefix.0, prefix.1 etc
   */
  protected def readSequence[T](prefix: String, container: Manifest[_], contained: Manifest[T]) = {
    val builder =
      Try {
        if (container.runtimeClass.isArray)
          mutable.ArrayBuilder.make()(contained)
        else {
          try {
            // try to construct a builder by going through the companion
            container.runtimeClass.newInstance().asInstanceOf[Iterable[T]].companion.newBuilder[T]
          } catch {
            case e: Exception => // hope the companion is named like we want...
              try {
                Class.forName(container.runtimeClass.getName + "$").getField("MODULE$").get(null).asInstanceOf[GenericCompanion[Iterable]].newBuilder[T]
              } catch {
                case e: Exception =>
                  throw new NoParameterException("Can't figure out what to do with a sequence of type:" + container, prefix)
              }
          }
        }
      }
    builder.map(bldr => {
      var touched = Set.empty[String]
      Stream.from(0).map(i => {
        readInTouched(Configuration.wrap(prefix, i.toString))(contained)
      }).takeWhile(_.isSuccess).map(_.get)
      .foreach({ case (el,t) => bldr += el; touched ++= t })
      bldr.result() -> touched
    })
  }
}

