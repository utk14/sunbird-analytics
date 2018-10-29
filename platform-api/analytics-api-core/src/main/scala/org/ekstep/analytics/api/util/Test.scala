package org.ekstep.analytics.api.util

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.{ QueryBuilder => QB }
import org.ekstep.analytics.framework.conf.AppConf
import org.ekstep.analytics.api.Constants

object Test extends App {

    val embeddedCassandra = AppConf.getConfig("cassandra.service.embedded.enable").toBoolean
    val host = AppConf.getConfig("spark.cassandra.connection.host")
    val port = if (embeddedCassandra) AppConf.getConfig("cassandra.service.embedded.connection.port").toInt else 9042

    println("embeddedCassandra: " + embeddedCassandra)
    println("host: " + host)
    println("port: " + port)

    val cluster = {
        Cluster.builder()
            .addContactPoint(host)
            .withPort(port)
            .build()
    }
    val session = cluster.connect()
    val query = QB.select().from(Constants.PLATFORML_DB, Constants.JOB_REQUEST).allowFiltering().where(QB.eq("request_id", "requestId")).and(QB.eq("client_key", "clientKey"))
    val resultSet = session.execute(query)
    println(session.getLoggedKeyspace)
    session.close
}