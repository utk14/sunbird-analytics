package org.ekstep.analytics.util

import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, Response}
import com.typesafe.config.Config

import scala.concurrent.duration._

class ElasticsearchUtil(config: Config) {

    private val SEARCH_SERVICE_URL = config.getString("service.search.url")
    private val BulkFetchLimit = config.getInt("search.service.bulk.limit")
    private val client = ElasticClient(ElasticProperties(SEARCH_SERVICE_URL))
    private var scrollId = ""
    private var counter: Int = 0

    def scrollQuery(indexName: String, queryString: String): Option[Response[SearchResponse]] = {
        import com.sksamuel.elastic4s.http.ElasticDsl._
        try {
            val response: Response[SearchResponse] = client.execute {
                search(indexName).rawQuery(queryString).limit(BulkFetchLimit).scroll(2 minute)
            }.await
            scrollId = response.result.scrollId.get
            counter += response.result.hits.hits.length
            println(s"[$indexName]: fetched total no. records ===> $counter")
            Option(response)
        } catch {
            case ex: Exception => {
                ex.printStackTrace
                None
            }
        }
    }

    def getNextBatch: Option[Response[SearchResponse]] = {
        import com.sksamuel.elastic4s.http.ElasticDsl._
        try {
            val response: Response[SearchResponse] = client.execute {
                searchScroll(scrollId,"2m")
            }.await
            counter += response.result.hits.hits.length
            println(s"getting next batch, total no. of records ===> $counter")
            if (response.result.hits.hits.length == 0) {
                counter = 0
                deleteScrolls(List(scrollId))
                None
            } else {
                Option(response)
            }
        } catch {
            case ex: Exception => {
                ex.printStackTrace
                None
            }
        }
    }

    def deleteScrolls(ids: List[String]): Unit = {
        import com.sksamuel.elastic4s.http.ElasticDsl._
        try {
            client.execute {
                clearScroll(ids)
            }.await
            scrollId = ""
            println(s"Deleted scroll ids created for the scroll query!")
        } catch {
            case ex: Exception => ex.printStackTrace
        }
    }
}