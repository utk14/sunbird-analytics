package org.ekstep.analytics.jobs

import org.ekstep.analytics.util.CommonUtil
import com.datastax.spark.connector._

object UserDataUpdater {

    implicit val className = "org.ekstep.analytics.jobs.UserDataUpdater"
    val cassandraHost = "localhost"
    val userKeyspace = "sunbird"
    val userTableName = "user"

    val sc = CommonUtil.getSparkContext(8, className, Option(cassandraHost));

    def updateUserMetaDataToRedis: Unit = {
        val userData = sc.cassandraTable(userKeyspace, userTableName)
        // convert to key:fieldsMap and write to redis
    }
}
