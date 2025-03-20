package com.github.catvod.spider


import com.github.catvod.bean.Class
import com.github.catvod.bean.Result
import com.github.catvod.bean.Vod

import com.github.catvod.crawler.Spider
import com.github.catvod.crawler.SpiderDebug
import com.github.catvod.net.OkHttp
import com.github.catvod.utils.Json
import com.github.catvod.utils.Util
import com.google.gson.JsonArray
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import org.jsoup.Jsoup
import java.util.*

class Glod : Spider() {
    private val host = Util.base64Decode("aHR0cHM6Ly93d3cuY2Zrajg2LmNvbS8=")

    private val epUrl = "/api/mw-movie/anonymous/v1/video/episode/url?id=%s&nid=%s"

    private val deviceId = UUID.randomUUID().toString();

    private val classList = listOf(Class("1", "电影"), Class("2", "电视剧"), Class("4", "动漫"), Class("3", "综艺"))

    override fun homeContent(filter: Boolean): String {
        val string = OkHttp.string(host, Util.webHeaders("https://www.bing.com"))
        val vodList = parseFromJson(string, "home")
//        val vodList = parseVodList(string)
        return Result.string(classList, vodList)
    }

    override fun detailContent(ids: MutableList<String>): String {
        val url = host + ids[0]
        val string = OkHttp.string(url, Util.webHeaders(host))
        val parse = Jsoup.parse(string)
        val name = parse.select("h1.title").text()
        val img = parse.select("div[class^=detail__CardImg] img").attr("src")
        val tag = parse.select("div.tags > a.tag").eachText().joinToString(" ")
        val vod = Vod(ids[0], name, img, tag)
        val director = parse.select("div.director")
        val d = director[0].select("a").text()
        val actor = director[1].select("a").eachText().joinToString(" ")
        vod.setVodActor(actor)
        vod.setVodDirector(d)
        val desc = parse.select("div.intro div.wrapper_more_text").text()
        vod.vodContent = desc

        val linkList = parse.select("div.listitem > a")

        val playUrlList = mutableListOf<Vod.VodPlayBuilder.PlayUrl>()
        for (element in linkList) {
            val u = element.attr("href")
            val n = element.text()
            playUrlList.add(Vod.VodPlayBuilder.PlayUrl().also {
                it.name = n
                it.url = u
            })
        }

        val buildResult = Vod.VodPlayBuilder().append("glod", playUrlList).build()

        val time = parse.select("div.item:contains(上映时间)").select(".item-top").text()
        vod.setVodYear(DateFormatUtils.format(DateUtils.parseDate(time, "yyyy-MM-dd"), "yyyy"))
        vod.setVodPlayFrom(buildResult.vodPlayFrom)
        vod.vodPlayUrl = buildResult.vodPlayUrl
        return Result.string(vod)
    }

    /**
     * 请求头
     * t 时间戳
     * sign 签名
     * deviceId
     * authorization 空的
     * 还有cookie
     */
    override fun playerContent(flag: String, id: String, vipFlags: MutableList<String>): String {
        val list = id.split("/")
        val i = list[3]
        val nid = list[5]
        val webHeaders = Util.webHeaders(host)
        val time = Date().time.toString()
        val sign = Util.sha1Hex(
            Util.MD5("id=${i}&nid=${nid}&key=cb808529bae6b6be45ecfab29a4889bc&t=${time}")
        )
        webHeaders["t"] = time
        webHeaders["deviceId"] = deviceId
        webHeaders["Sign"] = sign

        val string = OkHttp.string(host + String.format(epUrl, i, nid), webHeaders)
        val parse = Json.parse(string).asJsonObject
        if (parse.get("code").asInt != 200) {
            SpiderDebug.log("glod 获取播放链接失败:$string")
            return Result.error("获取播放链接失败")
        }
        val url = parse.get("data").asJsonObject.get("playUrl").asString
        return Result.get().url(url).string()
    }

    override fun categoryContent(tid: String, pg: String, filter: Boolean, extend: HashMap<String, String>): String {
        val url = "$host/type/$tid"
        val string = OkHttp.string(url, Util.webHeaders(host))
        val vodList = parseFromJson(string, "cate")
        return Result.string(classList, vodList)
    }

    override fun searchContent(key: String, quick: Boolean): String {
        val string = OkHttp.string("${host}vod/search/$key", Util.webHeaders(host))
        val vodList = parseFromJson(string, "search")
        return Result.string(vodList)
    }

    private fun parseFromJson(string: String, type: String): List<Vod> {
        val vodList = mutableListOf<Vod>()
        val parse = Jsoup.parse(string)
        val select = parse.select("script")
        val data = select.find {
            it.html().contains("操作成功")
        }
        if (data == null) {
            SpiderDebug.log("glod 找不到json")
            return vodList
        }
        val json = data.html().replace("self.__next_f.push(", "").replace(")", "")

        val gson = Json.parse(json).asJsonArray.get(1).asString.replace("6:", "")
        val resp = Json.parse(gson).asJsonArray.get(3).asJsonObject
        if (type == "home") {
            val element = resp.get("children").asJsonArray.get(3).asJsonObject.get("data").asJsonObject.get("data")
            var vList = element.asJsonObject.get("homeNewMoviePageData").asJsonObject.get("list").asJsonArray
            getVodList(vList, vodList)
            vList = element.asJsonObject.get("homeBroadcastPageData").asJsonObject.get("list").asJsonArray
            getVodList(vList, vodList)
            vList = element.asJsonObject.get("homeManagerPageData").asJsonObject.get("list").asJsonArray
            getVodList(vList, vodList)
            vList = element.asJsonObject.get("newestTvPageData").asJsonObject.get("list").asJsonArray
            getVodList(vList, vodList)
            vList = element.asJsonObject.get("newestCartoonPageData").asJsonObject.get("list").asJsonArray
            getVodList(vList, vodList)
        } else if (type == "cate") {
            for (jsonElement in resp.get("children").asJsonArray.get(3).asJsonObject.get("data").asJsonArray) {
                val objList = jsonElement.asJsonObject.get("vodList").asJsonObject.get("list").asJsonArray
                getVodList(objList, vodList)
            }
        } else if (type == "search") {
            val asJsonArray =
                resp.get("data").asJsonObject.get("data").asJsonObject.get("result").asJsonObject.get("list").asJsonArray
            getVodList(asJsonArray, vodList)
        }
        return vodList
    }

    private fun getVodList(
        objList: JsonArray, vodList: MutableList<Vod>
    ) {
        for (oj in objList) {
            val obj = oj.asJsonObject
            val v = Vod()
            v.setVodId("/detail/" + obj.get("vodId").asString)
            v.setVodName(obj.get("vodName").asString)
            //            v.setVodActor(obj.get("vodActor").asString)
            v.setVodRemarks(obj.get("vodScore").asString)
            v.setVodPic(obj.get("vodPic").asString)
            vodList.add(v)
        }
    }

    private fun parseVodList(string: String): MutableList<Vod> {
        val parse = Jsoup.parse(string)
        val list = parse.select("div.content-card")
        val vodList = mutableListOf<Vod>()
        for (element in list) {
            val id = element.select("a").attr("href")
            val title = element.select("div.info-title-box > div.title").text()
            val score = element.select("div.bottom div[class^=score]").text()
            val img = element.select("img").attr("srcset")
            vodList.add(Vod(id, title, host + img, score))
        }
        return vodList
    }
}