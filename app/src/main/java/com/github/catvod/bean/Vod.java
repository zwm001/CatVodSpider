package com.github.catvod.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Vod {

    @SerializedName("type_name")
    private String typeName;
    @SerializedName("vod_id")
    private String vodId;
    @SerializedName("vod_name")
    private String vodName;
    @SerializedName("vod_pic")
    private String vodPic;
    @SerializedName("vod_remarks")
    private String vodRemarks;
    @SerializedName("vod_year")
    private String vodYear;
    @SerializedName("vod_area")
    private String vodArea;
    @SerializedName("vod_actor")
    private String vodActor;
    @SerializedName("vod_director")
    private String vodDirector;
    @SerializedName("vod_content")
    private String vodContent;
    @SerializedName("vod_play_from")
    private String vodPlayFrom;
    @SerializedName("vod_play_url")
    private String vodPlayUrl;
    @SerializedName("vod_tag")
    private String vodTag;
    @SerializedName("action")
    private String action;
    @SerializedName("style")
    private Style style;

    public static Vod objectFrom(String str) {
        Vod item = new Gson().fromJson(str, Vod.class);
        return item == null ? new Vod() : item;
    }

    public static Vod action(String action) {
        Vod vod = new Vod();
        vod.action = action;
        return vod;
    }

    public Vod() {
    }

    public Vod(String vodId, String vodName, String vodPic) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
    }

    public Vod(String vodId, String vodName, String vodPic, String vodRemarks) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
    }

    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, String action) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setAction(action);
    }

    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, Style style) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setStyle(style);
    }

    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, Style style, String action) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setStyle(style);
        setAction(action);
    }

    public Vod(String vodId, String vodName, String vodPic, String vodRemarks, boolean folder) {
        setVodId(vodId);
        setVodName(vodName);
        setVodPic(vodPic);
        setVodRemarks(vodRemarks);
        setVodTag(folder ? "folder" : "file");
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public void setVodRemarks(String vodRemarks) {
        this.vodRemarks = vodRemarks;
    }

    public void setVodYear(String vodYear) {
        this.vodYear = vodYear;
    }

    public void setVodArea(String vodArea) {
        this.vodArea = vodArea;
    }

    public void setVodActor(String vodActor) {
        this.vodActor = vodActor;
    }

    public void setVodDirector(String vodDirector) {
        this.vodDirector = vodDirector;
    }

    public void setVodContent(String vodContent) {
        this.vodContent = vodContent;
    }

    public String getVodContent() {
        return vodContent;
    }

    public void setVodPlayFrom(String vodPlayFrom) {
        this.vodPlayFrom = vodPlayFrom;
    }

    public void setVodPlayUrl(String vodPlayUrl) {
        this.vodPlayUrl = vodPlayUrl;
    }

    public String getVodPlayUrl() {
        return vodPlayUrl;
    }

    public void setVodTag(String vodTag) {
        this.vodTag = vodTag;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public static class Style {

        @SerializedName("type")
        private String type;
        @SerializedName("ratio")
        private Float ratio;

        public static Style rect() {
            return rect(0.75f);
        }

        public static Style rect(float ratio) {
            return new Style("rect", ratio);
        }

        public static Style oval() {
            return new Style("oval", 1.0f);
        }

        public static Style full() {
            return new Style("full");
        }

        public static Style list() {
            return new Style("list");
        }

        public Style(String type) {
            this.type = type;
        }

        public Style(String type, Float ratio) {
            this.type = type;
            this.ratio = ratio;
        }
    }
    public static class VodPlayBuilder{
        private List<String> vodPlayFrom = new ArrayList<String>();
        private List<String> vodPlayUrl = new ArrayList<String>();

        /**
         * 格式： from name1$$$name2$$$name3
         *       url name$url#name2$url2$$$(分类2)$$分类3
         * @param playFrom
         * @param playUrl
         */
        public VodPlayBuilder append(String playFrom, List<PlayUrl> playUrl){
            vodPlayFrom.add(playFrom);
            vodPlayUrl.add(toPlayUrlStr(playUrl));
            return this;
        }

        public BuildResult build(){
            BuildResult buildResult = new BuildResult();
            buildResult.vodPlayFrom = String.join("$$$", vodPlayFrom);
            buildResult.vodPlayUrl = String.join("$$$", vodPlayUrl);
            return buildResult;
        }

        private String toPlayUrlStr(List<PlayUrl> playUrl) {
            List<String> list = new ArrayList<>();
            for (PlayUrl url : playUrl) {
                list.add(url.name.replace("m3u8", "") + '$' + url.url);
            }
            return String.join("#", list);
        }

        public static class BuildResult{
            public String vodPlayFrom;
            public String vodPlayUrl;
        }

        public static class PlayUrl {
            public String flag; // 线路标志
            public String name;
            public String url;
        }
    }
}
