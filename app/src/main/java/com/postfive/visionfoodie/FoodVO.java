package com.postfive.visionfoodie;

import java.io.Serializable;

public class FoodVO implements Serializable {
    private String keyword;
    private String url;
    private String title;
    private String brod_date;
    private String name;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrod_date() {
        return brod_date;
    }

    public void setBrod_date(String brod_date) {
        this.brod_date = brod_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
