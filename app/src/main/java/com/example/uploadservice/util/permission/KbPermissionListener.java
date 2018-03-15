package com.example.uploadservice.util.permission;

/**
 * Description：
 * Created by kang on 2017/10/26.
 */
public interface KbPermissionListener {

    /**
     * 授权
     */
    void onPermit(int requestCode, String... permission);

    /**
     * 未授权
     */
    void onCancel(int requestCode, String... permission);
}
