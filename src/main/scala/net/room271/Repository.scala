package net.room271

import org.elasticsearch.search.SearchHit
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse

import scala.concurrent.{ExecutionContext, Future}

object Repository {

  import scala.concurrent.ExecutionContext.Implicits.global

  val basePath = Config.basePath

  def get(id: String): Future[Option[JObject]] = {
    Elasticsearch.get(id) map {
      case found if found.isExists => {
        val json = parse(found.getSourceAsString) match {
          case obj: JObject => Option(obj)
          case _ => None // TODO log this
        }
        json map (j => addApiData(id, basePath, j))
      }
      case notFound => None
    }
  }

  def getAll(): Future[JArray] = {
    val results = Elasticsearch.getAll() map { sr =>
      sr.getHits.getHits.toList flatMap hitToJson
    }

    results map JArray
  }

  private[this] def hitToJson(hit: SearchHit): Option[JObject] = {
    val id = hit.id
    val json = parse(hit.getSourceAsString) match {
      case obj: JObject => Option(obj)
      case _ => None // TODO log this
    }

    json map (j => addApiData(id, basePath, j))
  }

  private[this] def addApiData(id: String, basePath: String, json: JObject): JObject = {
    val idField = ("id", id)
    val urlField = ("url", basePath + "/recipes/" + id)
    val newFields = urlField ~ idField

    newFields merge json
  }
}
