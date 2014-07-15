package net.room271

import net.room271.QueryStringParser.{GreaterThan, LessThan, SearchFilters}
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders.{filteredQuery, matchAllQuery}

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
    val queryFilters = queryString map QueryStringParser.parse map filtersFromSearchFilters

    val response = client.prepareSearch("recipes")
      .setTypes("recipe")

    queryFilters foreach (qfs => response.setQuery(filteredQuery(matchAllQuery, qfs)))

    toFuture(response.execute())
  }

  private[this] def filtersFromSearchFilters(searchFilters: SearchFilters) = {
    val builder = andFilter()

    if (searchFilters.q.nonEmpty) {
      // TODO better to AND the arguments and then search?
      builder.add(termsFilter("description", searchFilters.q :_*))
    }

    searchFilters.ingredient map { ingredient =>
      builder.add(termFilter("ingredient", ingredient))
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
