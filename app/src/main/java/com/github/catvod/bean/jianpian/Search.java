package com.github.catvod.bean.jianpian;

import android.text.TextUtils;

import com.github.catvod.bean.Vod;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Search {
    public static Search objectFrom(String str) {
        return new Gson().fromJson(str, Search.class);
    }

    @SerializedName("data")
    private List<Search> data;
    @SerializedName("id")
    private String id;
    @SerializedName(value = "thumbnail", alternate = "path")
    private String thumbnail;
    @SerializedName("title")
    private String title;
    @SerializedName("mask")
    private String mask;

    public String getId() {
        return TextUtils.isEmpty(id) ? "" : id;
    }

    public String getThumbnail() {
        return TextUtils.isEmpty(thumbnail) ? "" : "http://img1.vbwus.com" + thumbnail;
    }

    public String getTitle() {
        return TextUtils.isEmpty(title) ? "" : title;
    }

    public String getMask() {
        return TextUtils.isEmpty(mask) ? "" : mask;
    }

    public Vod vod() {
        return new Vod(getId(), getTitle(), getThumbnail(), getMask());
    }

    public List<Search> getData() {
        return data == null ? Collections.emptyList() : data;
    }
}
