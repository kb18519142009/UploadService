package com.example.uploadservice.adapter;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.uploadservice.R;
import com.example.uploadservice.model.Topic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Description: 视频列表适配器
 */
public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "VideoListAdapter";
    private static final int IMG_CAMERA = 0;
    private static final int IMG_VIDEO = 1;

    private Application mApp;
    private Context mContext;

    private List<Topic> mList = new ArrayList<>();

    private int mCheckPosition = 1;

    private OnItemClickListener mOnItemClickListener;

    private OnVideoRecordListener onVideoRecordListener;

    public VideoListAdapter(Context context) {
        this.mContext = context;
        mApp = (Application) mContext.getApplicationContext();
    }

    /**
     * 对外提供的设置数据的方法
     *
     * @param list
     */
    public void addData(List<Topic> list) {
        mList.clear();
        this.mList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IMG_CAMERA) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_photo_info_camera, parent, false);
            return new VideoCameraViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_info, parent, false);
            return new VideoListViewHolder(view);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == IMG_VIDEO) {
            VideoListViewHolder vh = (VideoListViewHolder) holder;
            vh.mThumb.setBackgroundColor(android.graphics.Color.parseColor("#222222"));

            //用Glide加载视频第一帧，速度快，效果不错
            RequestOptions options = new RequestOptions();
            // glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            options.override(mApp.getResources().getDisplayMetrics().widthPixels / 3, mApp.getResources().getDisplayMetrics().widthPixels / 3);
            // glide 缓存
            options.diskCacheStrategy(DiskCacheStrategy.ALL);
            // 裁剪中间部分显示
            options.fitCenter();
            // 默认占位图
            options.placeholder(R.drawable.img_placeholder_image_loading);
            Glide.with(mContext)
                    .asBitmap()
                    .load(Uri.fromFile(new File(mList.get(position - 1).getLocalVideoPath())))
                    .apply(options)
                    .into(vh.mThumb);

            vh.mThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            vh.mDuration.setText(mList.get(position - 1).getDuration());
            vh.mSelected.setVisibility(View.GONE);
            vh.mDuration.setVisibility(View.VISIBLE);
            if (mCheckPosition == position) {
                vh.mSelected.setVisibility(View.VISIBLE);
            }
            vh.mThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onSelected(mList.get(position - 1));
                        Log.e(TAG, "onClick: " + mList.get(position - 1).getLocalVideoPath());
                    }
//                    notifyItemChanged(mCheckPosition);
                    mCheckPosition = position;
                    notifyDataSetChanged();
//                    notifyItemChanged(mCheckPosition);
                }
            });
        }
    }

    public Topic getCheckPosition() {
        if (mCheckPosition >= 1 && mList.size() > 0) {
            return mList.get(mCheckPosition - 1);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return IMG_CAMERA;
        } else {
            return IMG_VIDEO;
        }
    }

    public interface OnItemClickListener {
        void onSelected(Topic videoItem);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnVideoRecordListener {
        void onVideoRecord();
    }

    public void setOnVideoRecordListener(OnVideoRecordListener onVideoRecordListener) {
        this.onVideoRecordListener = onVideoRecordListener;
    }

    class VideoListViewHolder extends RecyclerView.ViewHolder {

        ImageView mThumb;

        TextView mDuration;

        ImageView mSelected;

        public VideoListViewHolder(View itemView) {
            super(itemView);

            mThumb = itemView.findViewById(R.id.iv_thumb);

            mDuration = itemView.findViewById(R.id.tv_duration);

            mSelected = itemView.findViewById(R.id.iv_selected);
        }
    }

    class VideoCameraViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView mPhoto;

        public VideoCameraViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.iv_photo)
        void clickedCamera() {
            if (onVideoRecordListener != null) {
                onVideoRecordListener.onVideoRecord();
            }
        }
    }
}
