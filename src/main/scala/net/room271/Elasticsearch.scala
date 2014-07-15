package net.room271

import net.room271.QueryStringParser.{GreaterThan, LessThan, SearchFilters}
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.{MatchQueryBuilder, AndFilterBuilder}
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._

import scala.concurrent.{Future, Promise}

object Elasticsearch {

  val client = new TransportClient()
    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300))

  def get(id: String): Future[GetResponse] = {
    val response = client.prepareGet("recipes", "recipe", id)
      .execute()

    toFuture(response)
  }

  def getAll(): Future[SearchResponse] = {
    val response = client.prepareSearch("recipes")
      .setTypes("recipe")
      .execute()

    toFuture(response)
  }

  def search(queryString: Option[String]): Future[SearchResponse] = {
    val searchFilters = queryString map QueryStringParser.parse
    val filters = searchFilters map filtersFromSearchFilters getOrElse matchAllFilter
    val queries = searchFilters map queriesFromSearchFilters getOrElse matchAllQuery

    val response = client.prepareSearch("recipes")
      .setTypes("recipe")
      .setQuery(filteredQuery(queries, filters))
      .execute

    toFuture(response)
  }

  private[this] def queriesFromSearchFilters(searchFilters: SearchFilters) = {
    searchFilters.q match {
      case Nil => matchAllQuery()
      case some => {
        multiMatchQuery(some.mkString(" "), "name", "description")
          .operator(MatchQueryBuilder.Operator.AND)
      }
    }
  }

  private[this] def filtersFromSearchFilters(searchFilters: SearchFilters) = {
    val builder = andFilter()

    searchFilters.ingredient map { ingredient =>
      builder.add(termFilter("ingredients.name", ingredient))
    }

    searchFilters.maxCookTime map {
      case GreaterThan(value) => builder.add(rangeFilter("maxCookTime").gt(value))
      case LessThan(value) => builder.add(rangeFilter("maxCookTime").lt(value))
    }

    searchFilters.recipeCuisine map { cuisine =>
      builder.add(termFilter("cuisine", cuisine))
    }

    builder
  }

  private[this] def toFuture[A](result: ListenableActionFuture[A]): Future[A] = {
    val promise = Promise[A]()
    val listener = new ActionListener[A] {
      def onFailure(e: Throwable) = promise.failure(e)
      def onResponse(response: A) = promise.success(response)
    }

    result.addListener(listener)
    promise.future
  }
}
