package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerPageAdapter extends RecyclerView.Adapter<StickerPageAdapter.ViewHolder> implements View.OnClickListener {

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);
    }

    private final int childViewHeight;
    private final int childViewWidth;
    private final StickerPageAdapter.OnStickerClickListener onStickerClickListener;
    private List<StickerPanelItem> stickerPanelItems;
    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnLoading(R.drawable.ic_sticker_loading_image)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();

    public StickerPageAdapter(int childViewHeight, int childViewWidth, OnStickerClickListener onStickerClickListener) {
        this.childViewHeight = childViewHeight;
        this.childViewWidth = childViewWidth;
        this.onStickerClickListener = onStickerClickListener;
    }

    @Override
    public StickerPageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerPageAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.common_sticker_view, parent, false));
    }

    @Override
    public void onBindViewHolder(StickerPageAdapter.ViewHolder holder, int position) {
        if (stickerPanelItems == null) {
            return;
        }
        final ImageView stickerImageView = holder.stickerImageView;
        stickerImageView.setSoundEffectsEnabled(false);

        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        lp.height = childViewHeight;

        final StickerPanelItem stickerPanelItem = stickerPanelItems.get(position);
        if (stickerPanelItem.isSticker()) {
            String stickerImageUri = stickerPanelItem.getSticker().getStickerUri();
            ImageLoader.getInstance().displayImage(stickerImageUri, new ImageViewAware(stickerImageView), displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    int padding = HSDisplayUtils.dip2px(10);
                    view.setPadding(padding, padding, padding, padding);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    int padding = HSDisplayUtils.dip2px(5);
                    view.setPadding(padding, padding, padding, padding);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
            stickerImageView.setVisibility(View.VISIBLE);
            lp.width = childViewWidth;
            stickerImageView.setTag(stickerPanelItem.getSticker());
            stickerImageView.setOnClickListener(this);
            stickerImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            final Animation downAni = createScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f);
                            stickerImageView.startAnimation(downAni);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            final Animation upAni = createScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f);
                            stickerImageView.startAnimation(upAni);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        } else if (stickerPanelItem.isDivider()) {
            stickerImageView.setVisibility(View.GONE);
            stickerImageView.setClickable(false);
            lp.width = HSDisplayUtils.dip2px(20);
        } else {
            stickerImageView.setVisibility(View.GONE);
            stickerImageView.setClickable(false);
            lp.width = childViewWidth;
        }
        holder.itemView.setLayoutParams(lp);
    }

    private static Animation createScaleAnimation(final float fromX, final float toX, final float fromY, final float toY) {
        final ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(10);
        animation.setFillAfter(true);
        return animation;
    }

    @Override
    public int getItemCount() {
        if (stickerPanelItems != null) {
            return stickerPanelItems.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<StickerPanelItem> stickerPanelItems) {
        this.stickerPanelItems = stickerPanelItems;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag != null && tag instanceof Sticker && onStickerClickListener != null) {
            onStickerClickListener.onStickerClick((Sticker) tag);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView stickerImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            stickerImageView = (ImageView) itemView.findViewById(R.id.sticker_view);
        }
    }
}