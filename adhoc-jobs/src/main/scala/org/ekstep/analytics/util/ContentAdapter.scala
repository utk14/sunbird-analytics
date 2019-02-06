package org.ekstep.analytics.util


import scala.annotation.tailrec

case class Content(id: String, metadata: Map[String, AnyRef], tags: Option[Array[String]], concepts: Array[String]);
case class ContentModel(id: String, subject: List[String], contentType: String, languageCode: List[String], gradeList: List[String] = List[String]());
case class ContentResult(count: Int, content: Option[Array[Map[String, AnyRef]]])
case class ContentResponse(id: String, ver: String, ts: String, params: Params, responseCode: String, result: ContentResult)

// LP API Response Model
case class Params(resmsgid: Option[String], msgid: Option[String], err: Option[String], status: Option[String], errmsg: Option[String])
case class Result(content: Option[Map[String, AnyRef]], contents: Option[Array[Map[String, AnyRef]]], questionnaire: Option[Map[String, AnyRef]],
                  assessment_item: Option[Map[String, AnyRef]], assessment_items: Option[Array[Map[String, AnyRef]]], assessment_item_set: Option[Map[String, AnyRef]],
                  games: Option[Array[Map[String, AnyRef]]], concepts: Option[Array[String]], maxScore: Double, items: Option[Array[Map[String, AnyRef]]]);
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Result);

object ContentAdapter {

    val SEARCH_SERVICE_URL = "service.search.url"
    val COMPOSITE_SEARCH_URL = s"$SEARCH_SERVICE_URL" + "service.search.path"

    implicit val className = "org.ekstep.analytics.adapter.ContentAdapter"

    @tailrec
    def search(offset: Int, limit: Int, contents: Array[Map[String, AnyRef]], action: (Int, Int) => ContentResult): Array[Map[String, AnyRef]] = {
        val result = action(offset, limit)
        val c = contents ++ result.content.getOrElse(Array())
        if (result.count > (offset + limit)) {
            search((offset + limit), limit, c, action)
        } else {
            c
        }
    }


    def getPublishedContent(): Array[Map[String, AnyRef]] = {
        def _searchContent(offset: Int, limit: Int): ContentResult = {
            val searchUrl = COMPOSITE_SEARCH_URL
            val request = Map("request" -> Map("filters" -> Map("objectType" -> List("Content"), "contentType" -> List("Story", "Worksheet", "Collection", "Game"), "status" -> List("Draft", "Review", "Redraft", "Flagged", "Live", "Retired", "Mock", "Processing", "FlagDraft", "FlagReview")), "exists" -> List("lastPublishedOn", "downloadUrl"), "offset" -> offset, "limit" -> limit));
            val resp = RestUtil.post[ContentResponse](searchUrl, JSONUtils.serialize(request));
            resp.result;
        }

        search(0, 200, Array[Map[String, AnyRef]](), _searchContent);
    }

    /**
      * Which is used to get the total published contents list
      * @return ContentResult
      */
    def getPublishedContentList(): ContentResult = {
        val request =
            s"""
               |{
               |    "request": {
               |        "filters":{
               |          "contentType": "Resource"
               |        },
               |        "fields": ["identifier", "objectType", "resourceType"]
               |    }
               |}
             """.stripMargin
        RestUtil.post[ContentResponse](COMPOSITE_SEARCH_URL, request).result
    }

}