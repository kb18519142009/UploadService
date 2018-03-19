package com.example.uploadservice.model;

import java.io.Serializable;

/**
 * Description：
 * Created by kang on 2017/10/21.
 */
public class UploadVideoResult implements Serializable {
    /**
     * contentType : video/mp4
     * id : 3
     * url : http://daishu.com/dia.jpg
     * dimension : 600x652
     */

    private String contentType;
    private int id;
    private String url;
    private String dimension;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "UploadVideoResult{" +
                "contentType='" + contentType + '\'' +
                ", id=" + id +
                ", url='" + url + '\'' +
                ", dimension='" + dimension + '\'' +
                '}';
    }
}
