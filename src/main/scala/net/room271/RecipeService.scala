package net.room271

import akka.actor.Actor
import org.joda.time.Period
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.JsonMethods.parse
import spray.httpx.Json4sSupport
import spray.routing._

import scala.io.Source._

class RecipeServiceActor extends Actor with RecipeService {

  def actorRefFactory = context

  def receive = runRoute(routes)
}

trait RecipeService extends HttpService with Json4sSupport {

  implicit def executionContext = actorRefFactory.dispatcher
  implicit val json4sFormats = DefaultFormats ++ JodaTimeSerializers.all

  val startPoints = Map("recipes" -> (Config.basePath + "/recipes"))

  val basePath = "http://localhost:9000"

  val index = pathSingleSlash { complete { startPoints } }

  val recipes = path("recipes") { complete { Repository.getAll() } }

  val recipe = path("recipes" / Segment) { id => complete { Repository.get(id) } }

  val routes = get { index ~ recipes ~ recipe }
}

object RecipeService {

  case class Person(name: String)

  case class Recipe(
    url: String,
    id: String,
    name: String,
    description: String,
    image: String,
    prepTime: Period,
    cookTime: Period,
    totalTime: Period,
    ingredients: List[Map[String, String]],
    recipeCategory: String,
    recipeCuisine: String,
    recipeInstructions: String,
    nutrition: Map[String, String]
  )

  val cheeseOnToast = JsonLoader.loadJson("src/main/resources/recipes/cheese-on-toast.json")
  val chocolateCake = JsonLoader.loadJson("src/main/resources/recipes/chocolate-cake.json")
  val recipes = Map("123456" -> cheeseOnToast, "234567" -> chocolateCake)
}

object JsonLoader {

  def loadJson(path: String): JValue = parse(loadFile(path))

  def loadFile(path: String): String = fromFile(path).mkString
}