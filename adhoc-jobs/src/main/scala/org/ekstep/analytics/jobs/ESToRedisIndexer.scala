package org.ekstep.analytics.jobs

import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.{SparkConf, SparkContext}
import org.ekstep.analytics.util.{ElasticsearchUtil, JSONUtils, RedisUtil}
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException

import scala.annotation.tailrec

object ESToRedisIndexer {

    private val sc: SparkContext = new SparkContext(master="local[*]", appName = "my App", new SparkConf);
    private val config: Config = ConfigFactory.load
    private val elasticsearchUtil = new ElasticsearchUtil(config)
    private val redisIndex = config.getInt("redis.database.index")
    private val redisKeyProperty: String = config.getString("redis.database.key.property.from.source")

    def fetchAndIngest(): List[Boolean] = {

        val index = config.getString("search.service.query.index")
        val query = config.getString("search.service.query.jsonString")

        println(s"[$index] query ===> $query")
        require(!index.isEmpty && !query.isEmpty, "require valid inputs! index name and query cannot be empty!")

        getNext(elasticsearchUtil.scrollQuery(index, query))

        @tailrec
        def getNext(response: Option[Response[SearchResponse]]): Unit = {
            response match {
                case Some(data) => {
                    val _source = data.result.hits.hits
                    val mappedData: Array[Map[String, String]] = _source.map { obj =>
                        val source = obj.sourceAsMap
                        Map("id" -> source(redisKeyProperty).asInstanceOf[String] , "data" -> JSONUtils.serialize(source))
                    }
                    ingest(mappedData)
                    val batchResponse = elasticsearchUtil.getNextBatch
                    batchResponse match {
                        case Some(res) => {
                            if (res.result.hits.hits.nonEmpty) getNext(batchResponse)
                        }
                        case None => println("Fetched all records!")
                    }
                }
            }
        }
        List(true)
    }



    def ingest(data: Array[Map[String, String]]) = {
        var connection: Jedis = null
        try {
            connection = RedisUtil.getConnection
            val pipeline = connection.pipelined
            pipeline.select(redisIndex)
            data.foreach { obj =>
                pipeline.set(obj("id"), obj("data"))
            }
            pipeline.sync
        } catch {
            case ex: JedisException => {
                ex.printStackTrace()
            }
        } finally {
            if (connection != null) connection.close()
        }
    }

    def main(args: Array[String]): Unit = {
        println(s"======> Starting Redis indexing job <=======")
        // trying out how to run this code in spark?
        sc.parallelize(fetchAndIngest)
    }
}
