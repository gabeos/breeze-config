package breeze.config

import breeze.config.ReflectionUtils._
import com.thoughtworks.paranamer.{ParameterNamesNotFoundException, AdaptiveParanamer}

import scala.reflect.{OptManifest, Manifest}
import scala.util.Try

/**
 * breeze-config
 * 10/6/14
 * @author Gabriel Schubiner <gabeos@cs.washington.edu>
 *
 *
 */
trait ReflectionParsing {
  self: Configuration =>

  // We have a static type, and a dynamic type.
  // The dynamic type will have to be inferred.
  // Some attempts are made to deal with generics
  protected def reflectiveReadIn[T: Manifest](prefix: String): Try[(T, Set[String])] = {
    val staticManifest = implicitly[Manifest[T]]

    val (dynamicClass: Class[_], touchedProperties) = {
      recursiveGetProperty(prefix) match {
        case Some((prop, propName)) =>
          // replace each . in time with a $, for inner classes.
          Class.forName(prop) -> Set(propName)
        case None => staticManifest.runtimeClass -> Set.empty[String]
      }
    }
    if (dynamicClass.getConstructors.isEmpty)
      throw new NoParameterException("Could not find a constructor for type " + dynamicClass.getName, prefix)

    val staticTypeVars: Seq[String] = staticManifest.runtimeClass.getTypeParameters.map(_.toString)
    val staticTypeVals: Seq[OptManifest[_]] = staticManifest.typeArguments
    val staticTypeMap: Map[String, OptManifest[_]] = (staticTypeVars zip staticTypeVals).toMap withDefaultValue NoManifest

    val dynamicTypeMap = solveTypes(staticTypeMap, staticManifest.runtimeClass, dynamicClass)
    Try {
          try {
            // pick a constructor and figure out what the parameters names are
            val ctor = dynamicClass.getConstructors.head
            val reader = new AdaptiveParanamer()
            val paramNames = reader.lookupParameterNames(ctor)
            // Also get their types
            val typedParams = ctor.getGenericParameterTypes.map(mkManifest(dynamicTypeMap, _))
            // and defaults, where possible
            val defaults = lookupDefaultValues(dynamicClass, paramNames)
            val namedParams = for {((tpe, name), default) <- typedParams zip paramNames zip defaults} yield (tpe, name, default)
            val (paramValues, touched) = namedParams.map {
                                                           case (man, name, default) =>
                                                             readInTouched[Object](Configuration.wrap(prefix, name), default.get)(man)
                                                         }.unzip
            ctor.newInstance(paramValues: _*).asInstanceOf[T] -> touched.foldLeft(touchedProperties)(_ ++ _)
          } catch {
            case e: ParameterNamesNotFoundException =>
              throw new ConfigurationException("Could not find parameter names for " + dynamicClass.getName + " (" + prefix + ")") {
                def param = prefix
              }
          }
        }
  }

}
