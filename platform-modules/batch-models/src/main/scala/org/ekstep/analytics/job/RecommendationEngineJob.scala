package org.ekstep.analytics.job

import org.ekstep.analytics.framework.MeasuredEvent
import org.ekstep.analytics.model.RecommendationEngine
import org.ekstep.analytics.framework.JobDriver
import org.apache.spark.SparkContext

object RecommendationEngineJob extends optional.Application {

    def main(config: String)(implicit sc: Option[SparkContext] = None) {
        implicit val sparkContext: SparkContext = sc.getOrElse(null);
        JobDriver.run[MeasuredEvent]("batch", config, RecommendationEngine);
    }
}