package com.example.uploadservice.adapter;

import android.app.Application;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.uploadservice.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Description：
 * Created by kang on 2017/10/25.
 */
public class PhotoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PhotoListAdapter";
    private static final int IMG_CAMERA = 0;
    private static final int IMG_PICTURE = 1;

    private Application mApp;
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    private int mCheckPosition = 0;

    private OnItemClickListener mOnItemClickListener;

    private OnTakePhotoListener mOnTakePhotoListener;

    public PhotoListAdapter(Context context) {
        this.mContext = context;
        mApp = (Application) mContext.getApplicationContext();
    }

    /**
     * 对外提供的设置数据的方法
     *
     * @param list
     */
    public void addData(List<String> list) {
        mList.clear();
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IMG_CAMERA) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_photo_info_camera, parent, false);
            return new PhotoCameraViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_photo_info, parent, false);
            return new PhotoListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == IMG_PICTURE) {
            PhotoListViewHolder vh = (PhotoListViewHolder) holder;
            vh.mPhoto.setBackgroundColor(android.graphics.Color.parseColor("#ffffff"));

            RequestOptions options = new RequestOptions();
            // glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            options.override(mApp.getResources().getDisplayMetrics().widthPixels / 3, mApp.getResources().getDisplayMetrics().widthPixels / 3);
            // glide 缓存
            options.diskCacheStrategy(DiskCacheStrategy.ALL);
            // 裁剪中间部分显示
            options.centerCrop();
            // 默认占位图
            options.placeholder(R.drawable.img_placeholder_image_loading);
            Glide.with(mContext)
                    .asBitmap()
                    .load(mList.get(position - 1))
                    .apply(options)
                    .into(vh.mPhoto);

            vh.mSelected.setVisibility(View.GONE);
            if (mCheckPosition == position) {
                vh.mSelected.setVisibility(View.VISIBLE);
            }
            vh.mPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onSelected(mList.get(position - 1));
                        Log.e(TAG, "onClick: " + mList.get(position - 1));
                    }
                    mCheckPosition = position;
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 1 : mList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return IMG_CAMERA;
        } else {
            return IMG_PICTURE;
        }
    }

    public String getCheckPosition() {
        if (mCheckPosition >= 1 && mList.size() > 0) {
            return mList.get(mCheckPosition - 1);
        }
        return null;
    }

    public void changeCheckState() {
        mCheckPosition = 0;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onSelected(String photoPath);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnTakePhotoListener {
        void onTakePhoto();
    }

    public void setOnTakePhotoListener(OnTakePhotoListener onTakePhotoListener) {
        this.mOnTakePhotoListener = onTakePhotoListener;
    }

    class PhotoListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView mPhoto;
        @BindView(R.id.iv_selected)
        ImageView mSelected;

        public PhotoListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class PhotoCameraViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView mPhoto;

        public PhotoCameraViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.iv_photo)
        void clickedCamera() {
            if (mOnTakePhotoListener != null) {
                mOnTakePhotoListener.onTakePhoto();
            }
        }
    }
}
