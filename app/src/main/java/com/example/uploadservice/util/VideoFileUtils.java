package com.example.uploadservice.util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.example.uploadservice.model.Topic;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.List;

/**
 * Description: 视频文件工具类
 * Created by kang on 2017/10/26.
 */
public class VideoFileUtils {

    private static final String TAG = "VideoFileUtils";

    public static void getVideoFile(final List<Topic> list, File file) {// 获得视频文件
        final MediaMetadataRetriever media = new MediaMetadataRetriever();
        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // sdCard找到视频名称
                String name = file.getName();

                int i = name.lastIndexOf('.');//一定是找最后一个点出现的位置，否则无法找到名字中包含点的文件
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")) {
                        Topic videoItem = new Topic();
                        videoItem.setLocalVideoPath(file.getAbsolutePath());
                        try {
                            media.setDataSource(videoItem.getLocalVideoPath());
                            videoItem.setRotation(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));// 视频旋转方向
                            videoItem.setHeight(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); // 视频高度
                            videoItem.setWidth(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); // 视频宽度
                            videoItem.setDuration(DateUtils.stringForTime(Integer.parseInt(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));// 时长

                            list.add(videoItem);

                        } catch (Exception e) {
                            Log.e(TAG, "损坏的视频：" + videoItem.getLocalVideoPath());
                        }
                        return true;
                    }
                } else if (file.isDirectory()) {
                    getVideoFile(list, file);
                }
                return false;
            }
        });
    }

    public static void getVideoFile(final List<Topic> list, Context context) {// 获得视频文件
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Video.Media.MIME_TYPE + "=?"
                        ,
                        new String[]{"video/mp4"},
                        MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Topic videoItem = new Topic();
                videoItem.setLocalVideoPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))); // 路径
                media.setDataSource(videoItem.getLocalVideoPath());
                videoItem.setRotation(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));// 视频旋转方向
                String height = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);// 视频高度  
                String width = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);// 视频宽度  
                Log.e(TAG, "videopath: " + videoItem.getLocalVideoPath() + "  旋转方向: " + videoItem.getRotation() + "  高度: " + height + "  宽度: " + width);

                videoItem.setDuration(DateUtils.stringForTime(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))));// 时长
                list.add(videoItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
        }
        return size;
    }

}
