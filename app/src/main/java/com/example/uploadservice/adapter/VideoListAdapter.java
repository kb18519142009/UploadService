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

    private Application mApp;
    private Context mContext;

    private List<Topic> mList = new ArrayList<>();

    private int mCheckPosition = 0;

    private OnItemClickListener mOnItemClickListener;

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
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_info, parent, false);
        return new VideoListViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
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
                .load(Uri.fromFile(new File(mList.get(position).getLocalVideoPath())))
                .apply(options)
                .into(vh.mThumb);

        vh.mThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        vh.mDuration.setText(mList.get(position).getDuration());
        vh.mSelected.setVisibility(View.GONE);
        vh.mDuration.setVisibility(View.VISIBLE);
        if (mCheckPosition == position) {
            vh.mSelected.setVisibility(View.VISIBLE);
        }
        vh.mThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onSelected(mList.get(position));
                    Log.e(TAG, "onClick: " + mList.get(position).getLocalVideoPath());
                }
//                    notifyItemChanged(mCheckPosition);
                mCheckPosition = position;
                notifyDataSetChanged();
//                    notifyItemChanged(mCheckPosition);
            }
        });
    }

    public Topic getCheckPosition() {
        return mList.get(mCheckPosition);
    }

    public interface OnItemClickListener {
        void onSelected(Topic videoItem);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
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
}
