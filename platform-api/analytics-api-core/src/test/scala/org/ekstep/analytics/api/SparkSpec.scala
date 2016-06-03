package org.ekstep.analytics.api

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.ekstep.analytics.api.util.CommonUtil
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import org.json4s.jvalue2extractable
import org.json4s.string2JsonInput
import org.scalatest.BeforeAndAfterAll
import com.fasterxml.jackson.core.JsonParseException
import org.ekstep.analytics.api.util.JSONUtils

/**
 * @author Santhosh
 */
class SparkSpec extends BaseSpec with BeforeAndAfterAll {

    implicit var sc: SparkContext = null;

    override def beforeAll() {
        sc = CommonUtil.getSparkContext(1, "TestAnalyticsCore");
    }

    override def afterAll() {
        CommonUtil.closeSparkContext();
    }

    def loadFile[T](file: String)(implicit mf: Manifest[T]): RDD[T] = {
        if (file == null) {
            return null;
        }
        sc.textFile(file, 1).map { line => JSONUtils.deserialize[T](line) }.filter { x => x != null }.cache();
    }

}