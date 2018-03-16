package com.example.uploadservice.util;

import android.app.Application;
import android.content.Context;

/**
 * Description：
 * Created by kang on 2018/3/16.
 */

public class SizeUtils {
    /**
     * dp 转 px
     *
     * @param dpValue dp 值
     * @return px 值
     */
    public static int dp2px(Context context, final float dpValue) {
        final float scale = ((Application) context.getApplicationContext()).getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
