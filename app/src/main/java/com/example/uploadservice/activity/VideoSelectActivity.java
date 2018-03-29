package com.example.uploadservice.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.uploadservice.R;
import com.example.uploadservice.adapter.VideoListAdapter;
import com.example.uploadservice.adapter.decoration.ThreeGridDecoration;
import com.example.uploadservice.listener.PhotoAlbumListener;
import com.example.uploadservice.model.Topic;
import com.example.uploadservice.model.UploadVideoResp;
import com.example.uploadservice.net.ApiHelper;
import com.example.uploadservice.net.ApiInterface;
import com.example.uploadservice.upload.ProgressRequestBody;
import com.example.uploadservice.util.SizeUtils;
import com.example.uploadservice.util.SystemUtil;
import com.example.uploadservice.util.VideoFileUtils;
import com.example.uploadservice.util.permission.KbPermission;
import com.example.uploadservice.view.KbWithWordsCircleProgressBar;
import com.example.uploadservice.view.SquareRelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoSelectActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoRecordActivity";
    private static final int MSG_SHOW_VIDEO_LIST = 1000;
    private static final int MSG_HIDE_PLAY_PAUS_VIEW = 2000;
    private static final int MSG_DELAY_FIRST_FRAME = 3000;

    public static final int STATE_IDLE = 0; //通常状态
    public static final int STATE_PLAYING = 1; //视频正在播放
    public static final int STATE_PAUSED = 2; //视频暂停
    public static final int DEFAULT_SHOW_TIME = 3000; // 控制器的默认显示时间3秒

    @BindView(R.id.tv_next)
    TextView mTitleNext; // 下一步

    @BindView(R.id.tv_cancel)
    TextView mTvCancel; // 取消

    @BindView(R.id.iv_empty)
    ImageView mIvEmpty; //占位图

    @BindView(R.id.tv_empty)
    TextView mTvEmpty; //空白时文案

    @BindView(R.id.rl_video_play)
    SquareRelativeLayout mVideoPlay; //承载播放器的布局

    @BindView(R.id.rv_video_list)
    RecyclerView mVideoList; //视频列表

    @BindView(R.id.fl_loading)
    FrameLayout mLoading; //加载中

    //进度条相关
    @BindView(R.id.fl_circle_progress)
    ViewGroup mFlCircleProgress;

    @BindView(R.id.circle_progress)
    KbWithWordsCircleProgressBar mCircleProgress;

    private TextureView mTextureview; //更换为TextureView
    private Surface mSurface; //surface
    private ImageView mPlayPause; // 播放暂停按钮
    private ImageView mVideoBg; // 视频缩略图

    public List<Topic> mAllVideoList = new ArrayList<>(); // 视频信息集合
    private VideoListAdapter mVideoAdapter; // 视频列表适配器
    private MediaPlayer mMediaPlayer = new MediaPlayer(); // 播放器

    private Context mContext;
    private ApiInterface mApi;

    private int mCurState = STATE_IDLE; // 当前状态
    private boolean mPlayPuseIsShow = false; // 是否显示了播放暂停

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SHOW_VIDEO_LIST) {
                if (VideoSelectActivity.this.isFinishing()) {
                    return;
                }
                mLoading.setVisibility(View.GONE);
                if (mAllVideoList.size() <= 0) {
                    return;
                }

                mIvEmpty.setVisibility(View.GONE);
                mTvEmpty.setVisibility(View.GONE);

                addSurfaceView(mVideoPlay, mAllVideoList.get(0));
                Glide.with(mContext).load(new File(mAllVideoList.get(0).getLocalVideoPath())).into(mVideoBg);
                mVideoAdapter.addData(mAllVideoList);
            } else if (msg.what == MSG_HIDE_PLAY_PAUS_VIEW) {
                if (mMediaPlayer == null) return;
                if (mMediaPlayer.isPlaying()) {
                    mPlayPause.setVisibility(View.GONE);
                    mPlayPuseIsShow = false;
                } else {
                    mPlayPause.setVisibility(View.VISIBLE);
                    mPlayPuseIsShow = true;
                }
            } else if (msg.what == MSG_DELAY_FIRST_FRAME) {
                mVideoBg.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_select);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SystemUtil.setLightStatusBar(this, Color.WHITE);
        }
        Log.e(TAG, "onCreate: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        mContext = VideoSelectActivity.this;
        ButterKnife.bind(this);
        mApi = ApiHelper.getInstance().buildRetrofit(ApiHelper.BASE_URL)
                .createService(ApiInterface.class);

        initView();

        getData();

        initVideo();

        setListener();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mCurState = STATE_IDLE;
            mHandler.removeMessages(MSG_HIDE_PLAY_PAUS_VIEW);
            if (mPlayPause != null) {
                mPlayPause.setImageResource(R.drawable.img_play);
                mPlayPause.setVisibility(View.VISIBLE);
            }
            if (mVideoBg != null)
                mVideoBg.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     */
    private void initView() {

        mVideoList.setLayoutManager(new GridLayoutManager(this, 3));
        mVideoAdapter = new VideoListAdapter(mContext);
        mVideoList.setAdapter(mVideoAdapter);
        mVideoList.addItemDecoration(new ThreeGridDecoration(SizeUtils.dp2px(mContext, 2),
                SizeUtils.dp2px(mContext, 2)));
    }

    /**
     * 配置视频播放相关
     */
    private void initVideo() {

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 默认背景隐藏，播放按钮隐藏
                //mVideoBg.setVisibility(View.GONE);
                mPlayPause.setImageResource(R.drawable.img_pause);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPuseIsShow = true;
                Log.e(TAG, "onPrepared: 播放");
                mMediaPlayer.start();
                mCurState = STATE_PLAYING;
                //延迟隐藏第一帧图片
                mHandler.removeMessages(MSG_DELAY_FIRST_FRAME);
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_FIRST_FRAME, 300);
                //三秒消失播放按钮
                mHandler.removeMessages(MSG_HIDE_PLAY_PAUS_VIEW);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_PLAY_PAUS_VIEW, DEFAULT_SHOW_TIME);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                deleteSurfaceView(mVideoPlay);
                addSurfaceView(mVideoPlay, mVideoAdapter.getCheckPosition());
                initVideo();
//                mVideoBg.setImageBitmap(mVideoAdapter.getCheckPosition().getThumb());
                if (mVideoAdapter.getCheckPosition() != null) {
                    Glide.with(mContext).load(new File(mVideoAdapter.getCheckPosition().getLocalVideoPath())).into(mVideoBg);
                }
                // 移除已发送的消息
                mHandler.removeMessages(MSG_HIDE_PLAY_PAUS_VIEW);
                mVideoBg.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.img_play);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPuseIsShow = true;
                mCurState = STATE_IDLE;
            }
        });
    }

    /**
     * 设置监听
     */
    private void setListener() {
        mVideoAdapter.setPhotoAlbumListener(new PhotoAlbumListener<Topic>() {
            @Override
            public void onSelected(Topic topic) {
                if (topic == mVideoAdapter.getCheckPosition())
                    return;
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();

                    mCurState = STATE_IDLE;

                    deleteSurfaceView(mVideoPlay);
                    addSurfaceView(mVideoPlay, topic);
                    initVideo();
                }
                Glide.with(mContext).load(topic.getLocalVideoPath()).into(mVideoBg);
                // 默认背景显示，播放按钮显示
                mVideoBg.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.img_play);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPuseIsShow = true;
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mVideoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying() && mPlayPuseIsShow == true) {
                    mPlayPause.setVisibility(View.GONE);
                    mPlayPuseIsShow = false;
                } else if (mMediaPlayer.isPlaying() && mPlayPuseIsShow == false) {
                    mPlayPause.setVisibility(View.VISIBLE);
                    mPlayPuseIsShow = true;
                    mHandler.removeMessages(MSG_HIDE_PLAY_PAUS_VIEW);
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE_PLAY_PAUS_VIEW, DEFAULT_SHOW_TIME);
                }
            }
        });

        mTitleNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoAdapter.getCheckPosition() != null) {

                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mCurState = STATE_PAUSED;
                        mPlayPause.setImageResource(R.drawable.img_play);
                        mPlayPause.setVisibility(View.VISIBLE);
                        mPlayPuseIsShow = true;
                    }

                    uploadVideo();
                }
            }
        });
    }


    /**
     * 遍历  获取视频资源
     */
    private void getData() {
        mLoading.setVisibility(View.VISIBLE);
        // 获取本地相册内视频文件
        new Thread() {
            @Override
            public void run() {
                VideoFileUtils.getVideoFile(mAllVideoList, new File(Environment.getExternalStorageDirectory() /*+ "/DCIM"*/ + "/DCIM/Camera"));
//                VideoFileUtils.getVideoFile(mAllVideoList, new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()));
                if (mContext != null) {
                    mHandler.sendEmptyMessage(MSG_SHOW_VIDEO_LIST);
                }
            }
        }.start();
    }

    /**
     * 添加SurfaceView
     */
    private void addSurfaceView(RelativeLayout relativeLayout, Topic videoItem) {

        //mSurface = new SurfaceView(this);
        mTextureview = new TextureView(this);
        mTextureview.setSurfaceTextureListener(this);//设置监听函数  重写4个方法

        RelativeLayout.LayoutParams lp1 = null;
        // 特殊处理阿里云拍摄的视频
//        if (videoItem.getLocalVideoPath().contains("DaishuAli")) {
//            videoItem.setRotation("90");
//        }
        try {
            //先判断旋转方向再判断视频宽高度，确保适用于大多数视频
            if (videoItem == null) {
                lp1 = new RelativeLayout.LayoutParams(
                        SizeUtils.dp2px(mContext, 211), ViewGroup.LayoutParams.MATCH_PARENT
                );
            } else if (Integer.parseInt(videoItem.getRotation()) == 0 || Integer.parseInt(videoItem.getRotation()) == 180) {
                if (Integer.parseInt(videoItem.getHeight()) > Integer.parseInt(videoItem.getWidth())) {
                    lp1 = new RelativeLayout.LayoutParams(
                            SizeUtils.dp2px(mContext, 211), ViewGroup.LayoutParams.MATCH_PARENT
                    );
                } else if (Integer.parseInt(videoItem.getHeight()) == Integer.parseInt(videoItem.getWidth())) {
                    lp1 = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    );
                } else {
                    lp1 = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(mContext, 202)
                    );
                }
            } else if (Integer.parseInt(videoItem.getRotation()) == 90) {
                lp1 = new RelativeLayout.LayoutParams(
                        SizeUtils.dp2px(mContext, 211), ViewGroup.LayoutParams.MATCH_PARENT
                );
            } else if (Integer.parseInt(videoItem.getRotation()) == 270) {
                lp1 = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(mContext, 202)
                );
            } else {
                lp1 = new RelativeLayout.LayoutParams(
                        SizeUtils.dp2px(mContext, 211), ViewGroup.LayoutParams.MATCH_PARENT
                );
            }
            lp1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            relativeLayout.addView(mTextureview, lp1);

            mVideoBg = new ImageView(this);
            mVideoBg.setScaleType(ImageView.ScaleType.FIT_XY);
            relativeLayout.addView(mVideoBg, lp1);

            mPlayPause = new ImageView(this);
            mPlayPause.setImageResource(R.drawable.img_play);
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                    SizeUtils.dp2px(mContext, 30), SizeUtils.dp2px(mContext, 36)
            );

            mPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVideoAdapter.getCheckPosition() == null) {
                        Log.e(TAG, "onClick: " + mVideoAdapter.getCheckPosition().getLocalVideoPath());
                        return;
                    }
                    Log.e(TAG, "onClick: " + mCurState);
                    switch (mCurState) {

                        case STATE_PLAYING:
                            mMediaPlayer.pause();
                            mCurState = STATE_PAUSED;
                            mPlayPause.setImageResource(R.drawable.img_play);
                            break;
                        case STATE_PAUSED:
                            mMediaPlayer.start();
                            mCurState = STATE_PLAYING;
                            mPlayPause.setImageResource(R.drawable.img_pause);
                            mHandler.removeMessages(MSG_HIDE_PLAY_PAUS_VIEW);
                            mHandler.sendEmptyMessageDelayed(MSG_HIDE_PLAY_PAUS_VIEW, DEFAULT_SHOW_TIME);
                            break;
                        case STATE_IDLE:
                            try {
                                mMediaPlayer.reset();
                                Log.e(TAG, "onClick: STATE_IDLE");
                                mMediaPlayer.setSurface(mSurface);
                                mMediaPlayer.setDataSource(mVideoAdapter.getCheckPosition().getLocalVideoPath());
                                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mMediaPlayer.setScreenOnWhilePlaying(true);
                                mMediaPlayer.prepareAsync();

                            } catch (Exception e) {
                                Toast.makeText(VideoSelectActivity.this, "该视频无法播放，换一个吧~", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            });

            lp2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            relativeLayout.addView(mPlayPause, lp2);
        } catch (Exception e) {
            lp1 = new RelativeLayout.LayoutParams(
                    SizeUtils.dp2px(mContext, 211), ViewGroup.LayoutParams.MATCH_PARENT
            );
            lp1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            mVideoBg = new ImageView(this);
            mVideoBg.setBackgroundColor(getResources().getColor(R.color.black));
            relativeLayout.addView(mVideoBg, lp1);

            mPlayPause = new ImageView(this);
            mPlayPause.setImageResource(R.drawable.img_play);
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                    SizeUtils.dp2px(mContext, 30), SizeUtils.dp2px(mContext, 36)
            );

            mPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "该视频无法播放，换一个吧~", Toast.LENGTH_SHORT).show();
                }
            });
            mVideoBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "该视频无法播放，换一个吧~", Toast.LENGTH_SHORT).show();
                }
            });

            lp2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            relativeLayout.addView(mPlayPause, lp2);
        }
    }


    /**
     * 删除SurfaceView
     *
     * @param relativeLayout
     */
    private void deleteSurfaceView(RelativeLayout relativeLayout) {
        relativeLayout.removeAllViews();
    }

    private void uploadVideo() {
        mFlCircleProgress.setVisibility(View.VISIBLE);
        File file = new File(mVideoAdapter.getCheckPosition().getLocalVideoPath());
        //是否需要压缩
        //实现上传进度监听
        ProgressRequestBody requestFile = new ProgressRequestBody(file, "image/*", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.e(TAG, "onProgressUpdate: " + percentage);
                mCircleProgress.setProgress(percentage);
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

        mApi.uploadFile(body).enqueue(new Callback<UploadVideoResp>() {
            @Override
            public void onResponse(Call<UploadVideoResp> call, Response<UploadVideoResp> response) {
                mFlCircleProgress.setVisibility(View.GONE);
                UploadVideoResp resp = response.body();
                if (resp != null) {
                    Toast.makeText(mContext, "视频上传成功！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadVideoResp> call, Throwable t) {
                mFlCircleProgress.setVisibility(View.GONE);
                Toast.makeText(mContext, "视频上传失败，稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        KbPermission.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void finish() {
        super.finish();
        //Activity退出时动画
        overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_out_top);
    }
}
