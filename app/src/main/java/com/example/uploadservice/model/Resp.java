package com.example.uploadservice.model;

import java.io.Serializable;

/**
 * Resp
 * Created by kang on 2017/5/5.
 */
public class Resp implements Serializable {

    public static int STATUS_SUCCESS = 0;

    private int status;
    private String desc;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "Resp{" +
                "status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }
}
