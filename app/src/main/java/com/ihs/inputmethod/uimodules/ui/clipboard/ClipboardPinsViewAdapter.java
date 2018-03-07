package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;


public class ClipboardPinsViewAdapter extends RecyclerView.Adapter<ClipboardPinsViewAdapter.ViewHolder> {


    private DeleteFromPinsToRecentListener deleteFromPinsToRecentListener;
    private List<String> pinsDataList = new ArrayList<String>();
    private final String TAG = ClipboardPinsViewAdapter.class.getSimpleName();
    private Drawable deletePin;

    ClipboardPinsViewAdapter(List<String> list, DeleteFromPinsToRecentListener deleteFromPinsToRecentListener) {
        this.deleteFromPinsToRecentListener = deleteFromPinsToRecentListener;
        this.pinsDataList.addAll(list);
        HSLog.d(TAG, "create ClipboardPinsViewAdapter , pinsDataList  is " + pinsDataList.toString());
        deletePin = VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_delete_pin_item, null);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_pins_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String pinsContent = pinsDataList.get(holder.getAdapterPosition());
        holder.tv.setText(pinsContent);
        holder.tv.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getKeyTextColor(Color.WHITE));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(pinsContent);
            }
        });
        holder.img.setImageDrawable(deletePin);
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromPinsToRecentListener.deletePinsItem(pinsContent, holder.getAdapterPosition());
                HSLog.d(TAG, "  delete pins item  " + pinsContent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pinsDataList.size();
    }


    void insertDataChangeAndRefresh(String clipPinsData) {

        pinsDataList.add(0, clipPinsData);
        HSLog.d(TAG, "notifyDataSetChanged  recentAdapter,     current pinsDataList  is   " + clipPinsData.toString());
        notifyItemInserted(0);
    }

    void setPinsItemToTopAndRefresh(String clipPinsItem, int pinItemPosition) {
        deleteDataChangeAndRefresh(pinItemPosition);
        insertDataChangeAndRefresh(clipPinsItem);
    }

    void deleteDataChangeAndRefresh(int pinItemPosition) {
        pinsDataList.remove(pinItemPosition);
        notifyItemRemoved(pinItemPosition);
        if (pinItemPosition != pinsDataList.size()) { // 如果移除的是最后一个，忽略
            //对于被删掉的元素的后边的view进行重新onBindViewHolder
            notifyItemRangeChanged(pinItemPosition, pinsDataList.size() - pinItemPosition);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.pins_content);
            img = (ImageView) itemView.findViewById(R.id.pins_delete);
        }
    }


    public interface DeleteFromPinsToRecentListener {
        void deletePinsItem(String pinsContentItem, int position);
    }
}