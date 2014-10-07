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
  protected def readSequence[U, T[_]](prefix: String) //, container: Manifest[T[_]], contained: Manifest[U])
                                     (implicit cbf: CanBuildFrom[T[U], U, T[U]]): (Try[T[U]], Set[String]) = {
    val builder = cbf()
    //    Try {
    //      if(container.runtimeClass.isArray)
    //        mutable.ArrayBuilder.make()(contained)
    //      else {
    //        try {
    //          // try to construct a builder by going through the companion
    //          container.runtimeClass.newInstance().asInstanceOf[Iterable[U]].companion.newBuilder[U]
    //        } catch {
    //          case e: Exception => // hope the companion is named like we want...
    //            try {
    //              Class.forName(container.runtimeClass.getName + "$").getField("MODULE$").get(null).asInstanceOf[GenericCompanion[Iterable]].newBuilder[U]
    //            } catch {
    //              case e: Exception =>
    //                throw new NoParameterException("Can't figure out what to do with a sequence of type:" + container, prefix)
    //            }
    //        }
    //
    //      }
    //    }
    var ok = true
    var i = 0
    var touched = Set.empty[String]
    while (ok) {
      val (t, myTouched) = readInTouched(Configuration.wrap(prefix, i.toString)) //(contained)
      t.foreach(el => {
        builder += el
        touched ++= myTouched
      })

      t match {
        case Failure(e) => e match {
          case npe: NoParameterException => ok = false
          case _                         => return Failure(e) -> touched
        }
        case _          => Unit
      }

      i += 1
    }

    Success(builder.result()) -> touched
  }
}

trait ContainerParserMagnet[U, T[_]] {
  def apply(): (Try[T[U]], Set[String])
}

object ContainerParserMagnet {
  implicit def readSimpleCollection[U, T[_]](prefix: String)(implicit argp: ArgumentParser[U], cbf: CanBuildFrom[T[U], U, T[U]]) =
    new ContainerParserMagnet[U, T[U]] {
      def apply(): (Try[T[U]], Set[String]) = {
        val builder = cbf()

        var ok = true
        var i = 0
        var touched = Set.empty[String]
        while (ok) {
          val cfgKey = Configuration.wrap(prefix, i.toString)
          argp.parse(cfgKey) match {
            case Success(el) =>
              builder += el
              touched ++= cfgKey
            case Failure(e)  => e match {
              case npe: NoParameterException => ok = false
              case _                         => return Failure(e) -> touched
            }
          }
          i += 1
        }
        Success(builder.result()) -> touched
      }
    }

  implicit def readCollection[U,T[_]](prefix: String)(implicit argm: ArgParserMagnet, cbf: CanBuildFrom[T[U],U,T[U]]) =
    new ContainerParserMagnet[U,T[U]] {
      def apply(): (Try[T[U]], Set[String]) = {
        val builder = cbf()

        var ok = true
        var i = 0
        var touched = Set.empty[String]
        while (ok) {
          val cfgKey = Configuration.wrap(prefix, i.toString)
          argm() match {
            case Some(argp) => argp.parse(cfgKey)
            case None =>
          }



          match {
            case Success(el) =>
              builder += el
              touched ++= cfgKey
            case Failure(e)  => e match {
              case npe: NoParameterException => ok = false
              case _                         => return Failure(e) -> touched
            }
          }
          i += 1
        }
        Success(builder.result()) -> touched
      }
    }
}