package com.example.uploadservice.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uploadservice.R;
import com.example.uploadservice.upload.ProgressRequestBody;
import com.example.uploadservice.util.permission.KbPermission;
import com.example.uploadservice.util.permission.KbPermissionListener;
import com.example.uploadservice.util.permission.KbPermissionUtils;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Button mUploadPicture;
    private Button mUploadVideo;

    private Context mContext;

    private String mPicPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        TextView toolbarTitle = findViewById(R.id.tv_toolbar_title);
        toolbarTitle.setText(R.string.main_activity_title);
        mUploadPicture = findViewById(R.id.btn_upload_picture);
        mUploadPicture.setOnClickListener(this);
        mUploadVideo = findViewById(R.id.btn_upload_video);
        mUploadVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mUploadPicture) {
            if (KbPermissionUtils.needRequestPermission()) {
                KbPermission.with(this)
                        .requestCode(200)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .callBack(new KbPermissionListener() {
                            @Override
                            public void onPermit(int requestCode, String... permission) {
                                Intent uploadPictureIntent = new Intent(mContext, PictureSelectActivity.class);
                                startActivity(uploadPictureIntent);
                                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                            }

                            @Override
                            public void onCancel(int requestCode, String... permission) {
                                KbPermissionUtils.goSetting(mContext);
                            }
                        })
                        .send();
            } else {
                Intent uploadPictureIntent = new Intent(mContext, PictureSelectActivity.class);
                startActivity(uploadPictureIntent);
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        } else if (v == mUploadVideo) {

        }
    }

    private void uploadPicture() {
        File file = new File(mPicPath);

        //实现上传进度监听
        ProgressRequestBody requestFile = new ProgressRequestBody(file, "image/*", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.e(TAG, "onProgressUpdate: " + percentage);
            }

            @Override
            public void onError() {

            }

            @Override
            public void onFinish() {
            }
        });

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

//        mApiV1.uploadFile(body).enqueue(new RespCallback<UploadVideoResp>(mContext) {
//            @Override
//            public void onResp(@NonNull Call<UploadVideoResp> call, @NonNull Response<UploadVideoResp> response) {
//
//                UploadVideoResp resp = response.body();
//                if (resp != null) {
//                    submitFeedback(resp.getResult());
//                }
//            }
//
//            @Override
//            public void onFail(@NonNull Call<UploadVideoResp> call, @NonNull Throwable t) {
//                Log.e(TAG, "onFail: " + t.toString());
//                Toast.makeText(mContext, "上传失败，稍后重试", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
