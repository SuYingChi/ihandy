package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public final class StickerMyCardAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {
    private int imageWidth;
    private int imageHeight;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public StickerMyCardAdapterDelegate() {
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
    }

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).stickerGroup != null && items.get(position).isDownloaded;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new MyStickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 3;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        final StickerHomeModel stickerModel = items.get(position);
        final StickerGroup stickerGroup = stickerModel.stickerGroup;
        MyStickerCardViewHolder stickerCardViewHolder = (MyStickerCardViewHolder) holder;
        stickerCardViewHolder.stickerGroupName.setText(stickerGroup.getDownloadDisplayName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if (realImageUrl != null) {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
            ImageSize imageSize = new ImageSize(imageWidth, imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(stickerCardViewHolder.stickerRealImage), options, imageSize, null, null);
        }
    }

    public class MyStickerCardViewHolder extends RecyclerView.ViewHolder {
        View stickerCardView;
        ImageView moreMenuImage;

        TextView stickerGroupName;
        GifImageView stickerNewImage;
        ImageView stickerRealImage;


        public MyStickerCardViewHolder(View itemView) {
            super(itemView);

            stickerCardView = itemView.findViewById(R.id.sticker_card_view);
            stickerGroupName = (TextView) itemView.findViewById(R.id.sticker_name);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (GifImageView) itemView.findViewById(R.id.sticker_new_view);
            moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_image);

        }
    }

}