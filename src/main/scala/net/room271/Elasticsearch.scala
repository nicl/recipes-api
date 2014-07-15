package net.room271

import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.{SearchResponse, SearchType}
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

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
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .execute()

    toFuture(response)
  }

  def search() = ???

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
