package com.example.uploadservice.model;

/**
 * Description：
 * Created by kang on 2017/10/21.
 */
public class UploadVideoResp extends Resp{

    /**
     * result : {"contentType":"video/mp4","id":3,"url":"http://daishu.com/dia.jpg","dimension":"600x652"}
     */

    private UploadVideoResult result;

    public UploadVideoResult getResult() {
        return result;
    }

    public void setResult(UploadVideoResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "UploadVideoResp{" +
                "result=" + result +
                '}';
    }
}
