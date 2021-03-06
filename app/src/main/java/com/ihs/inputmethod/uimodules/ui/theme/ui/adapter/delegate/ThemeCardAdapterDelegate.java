package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.LockedCardActionUtils;

import java.util.List;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeCardAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

    public final static int TAG_DELETE = 0;
    public final static int TAG_MENU = 1;
    public final static int TAG_CARD = 2;
    public final static int TAG_DOWNLOAD = 3;

    private boolean themeAnalyticsEnabled;
    private View.OnClickListener cardViewOnClickListener;

    private RequestOptions requestOptions;

    public ThemeCardAdapterDelegate(boolean themeAnalyticsEnabled, View.OnClickListener cardViewOnClickListener) {
        this.themeAnalyticsEnabled = themeAnalyticsEnabled;
        this.cardViewOnClickListener = cardViewOnClickListener;
        Resources resources = HSApplication.getContext().getResources();
        int imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        int imageHeight = (int) (imageWidth / 1.6f);
        requestOptions = new RequestOptions().override(imageWidth, imageHeight);
    }

    @Override
    protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
        return items.get(position).keyboardTheme != null;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ThemeCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_card, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        ThemeCardViewHolder themeCardViewHolder = (ThemeCardViewHolder) holder;

        themeCardViewHolder.themeRealImage.setImageDrawable(null);
        ThemeHomeModel themeHomeModel = items.get(position);
        final HSKeyboardTheme keyboardTheme = themeHomeModel.keyboardTheme;
        holder.itemView.setTag(keyboardTheme.mThemeName);
        themeCardViewHolder.themeDelete.setVisibility(View.GONE);
        // show animated mark and new mark judgement
        boolean isShowAnimatedMark;
        isShowAnimatedMark = !(keyboardTheme.getThemeData() == null || keyboardTheme.getThemeData().get("showAnimatedMark") == null)
                && (boolean) keyboardTheme.getThemeData().get("showAnimatedMark");

        if (isShowAnimatedMark) {
            themeCardViewHolder.themeAnimatedImage.setVisibility(View.VISIBLE);
            themeCardViewHolder.themeNewImage.setVisibility(View.GONE);
        } else {
            themeCardViewHolder.themeAnimatedImage.setVisibility(View.GONE);

            if (HSThemeNewTipController.getInstance().isThemeNew(keyboardTheme.mThemeName)) {
                themeCardViewHolder.themeNewImage.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
                themeCardViewHolder.themeNewImage.setImageURI(uri);
            } else {
                themeCardViewHolder.themeNewImage.setVisibility(View.GONE);
            }
        }

        switch (keyboardTheme.getThemeType()) {
            case CUSTOM:
                themeCardViewHolder.themeName.setText(HSApplication.getContext().getString(R.string.theme_card_custom_theme_default_name));
                themeCardViewHolder.themeRealImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(keyboardTheme));
                themeCardViewHolder.themeDelete.setVisibility(themeHomeModel.deleteEnable ? View.VISIBLE : View.GONE);
                break;
            case BUILD_IN:
                themeCardViewHolder.themeRealImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(keyboardTheme));
                themeCardViewHolder.themeName.setText(keyboardTheme.getThemeShowName());
                break;
            case NEED_DOWNLOAD:
                if (LockedCardActionUtils.shouldLock(themeHomeModel)){
                    themeCardViewHolder.moreMenuImage.setImageResource(R.drawable.ic_theme_gift);
                }else {
                    themeCardViewHolder.moreMenuImage.setImageResource(R.drawable.ic_download_icon);
                }
            case DOWNLOADED:
                final String smallPreviewImgUrl = keyboardTheme.getSmallPreivewImgUrl();
                if (smallPreviewImgUrl != null) {
                    Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(smallPreviewImgUrl).into(themeCardViewHolder.themeRealImage);
                }
                themeCardViewHolder.themeName.setText(keyboardTheme.getThemeShowName());
                break;
        }

        ThemeHomeModel model = themeHomeModel;

        themeCardViewHolder.themeDelete.setTag(model);
        themeCardViewHolder.themeDelete.setTag(R.id.theme_card_view_tag_key_action, TAG_DELETE);
        themeCardViewHolder.themeDelete.setTag(R.id.theme_card_view_tag_key_position, position);
        themeCardViewHolder.themeDelete.setOnClickListener(cardViewOnClickListener);

        themeCardViewHolder.moreMenuImage.setTag(model);
        if (keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.NEED_DOWNLOAD) {
            themeCardViewHolder.moreMenuImage.setTag(R.id.theme_card_view_tag_key_action, TAG_DOWNLOAD);
        } else {
            themeCardViewHolder.moreMenuImage.setTag(R.id.theme_card_view_tag_key_action, TAG_MENU);
        }

        themeCardViewHolder.moreMenuImage.setTag(R.id.theme_card_view_tag_key_position, position);
        themeCardViewHolder.moreMenuImage.setOnClickListener(cardViewOnClickListener);

        themeCardViewHolder.themeCardView.setTag(model);
        themeCardViewHolder.themeCardView.setTag(R.id.theme_card_view_tag_key_action, TAG_CARD);
        themeCardViewHolder.themeCardView.setTag(R.id.theme_card_view_tag_key_position, position);
        themeCardViewHolder.themeCardView.setOnClickListener(cardViewOnClickListener);
    }


    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (themeAnalyticsEnabled) {
            final String theme = holder.itemView.getTag().toString();
            ThemeAnalyticsReporter.getInstance().recordThemeShown(theme);
        }
    }


}
