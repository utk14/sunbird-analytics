package org.ekstep.analytics.util

import org.apache.spark.{SparkConf, SparkContext}

object CommonUtil {

  def getSparkContext(parallelization: Int, appName: String, sparkCassandraConnectionHost: Option[AnyRef] = None): SparkContext = {
    val conf = new SparkConf().setAppName(appName);
    val master = conf.getOption("spark.master");
    // $COVERAGE-OFF$ Disabling scoverage as the below code cannot be covered as they depend on environment variables
    if (master.isEmpty) {
      conf.setMaster("local[*]");
    }

    if (!conf.contains("spark.cassandra.connection.host"))
      conf.set("spark.cassandra.connection.host", sparkCassandraConnectionHost.getOrElse("localhost").toString)

    // $COVERAGE-ON$
    val sc = new SparkContext(conf);
    sc;
  }
}
