package com.example

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

  implicit val json4sFormats = DefaultFormats ++ JodaTimeSerializers.all

  val basePath = "http://localhost:9000"

  val index = pathSingleSlash { complete { Map("recipes" -> (basePath + "/recipes")) } }

  val recipes = path("recipes") { complete { RecipeService.recipes.values } }

  val recipe = path("recipes" / IntNumber) { id => complete { RecipeService.recipes(id.toString) } }

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

  val recipes = Map("123456" -> cheeseOnToast)
}

object JsonLoader {

  def loadJson(path: String): JValue = parse(loadFile(path))

  def loadFile(path: String): String = fromFile(path).mkString
}