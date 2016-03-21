package org.ekstep.analytics.framework

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext


/**
 * @author Santhosh
 */
trait IBatchModel[T] {
    def execute(events: RDD[T], jobParams: Option[Map[String, AnyRef]])(implicit sc: SparkContext) : RDD[String]
}