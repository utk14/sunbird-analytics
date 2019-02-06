package org.ekstep.analytics.util

import org.apache.http.client.methods.{HttpGet, HttpPatch, HttpPost, HttpRequestBase}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import scala.io.Source

object RestUtil {

    implicit val className = "org.ekstep.analytics.util.RestUtil"

    private def _call[T](request: HttpRequestBase)(implicit mf: Manifest[T]) = {

        val httpClient = HttpClients.createDefault();
        try {
            val httpResponse = httpClient.execute(request);
            val entity = httpResponse.getEntity()
            val inputStream = entity.getContent()
            val content = Source.fromInputStream(inputStream, "UTF-8").getLines.mkString;
            inputStream.close
            if ("java.lang.String".equals(mf.toString())) {
                content.asInstanceOf[T];
            } else {
                JSONUtils.deserialize[T](content);
            }
        } finally {
            httpClient.close()
        }
    }

    def get[T](apiURL: String)(implicit mf: Manifest[T]) = {
        val request = new HttpGet(apiURL);
        request.addHeader("user-id", "analytics");
        try {
            _call(request.asInstanceOf[HttpRequestBase]);
        } catch {
            case ex: Exception =>
                ex.printStackTrace();
                null.asInstanceOf[T];
        }
    }

    def post[T](apiURL: String, body: String, requestHeaders: Option[Map[String, String]] = None)(implicit mf: Manifest[T]) = {

        val request = new HttpPost(apiURL)
        request.addHeader("user-id", "analytics")
        request.addHeader("Content-Type", "application/json")
        requestHeaders.getOrElse(Map()).foreach {
            case (headerName, headerValue) => request.addHeader(headerName, headerValue)
        }
        request.setEntity(new StringEntity(body))
        try {
            _call(request.asInstanceOf[HttpRequestBase])
        } catch {
            case ex: Exception =>
                ex.printStackTrace()
                null.asInstanceOf[T]
        }
    }

    def patch[T](apiURL: String, body: String, headers: Option[Map[String,String]] = None)(implicit mf: Manifest[T]) = {

        val request = new HttpPatch(apiURL);
        request.addHeader("user-id", "analytics");
        request.addHeader("Content-Type", "application/json");
        headers.getOrElse(Map).asInstanceOf[Map[String,String]].map { header =>
            request.addHeader(header._1, header._2)
        }
        request.setEntity(new StringEntity(body));
        try {
            _call(request.asInstanceOf[HttpRequestBase]);
        } catch {
            case ex: Exception =>
                ex.printStackTrace();
                null.asInstanceOf[T];
        }
    }

}