package com.ihs.inputmethod.uimodules.ui.emoji.Skin;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;

import java.util.List;

/**
 * Created by wenbinduan on 2016/11/22.
 */

public final class HSEmojiSkinViewAdapter extends RecyclerView.Adapter<HSEmojiSkinViewAdapter.ViewHolder> implements View.OnClickListener {

	public interface OnEmojiClickListener {
		void onEmojiSkinClick(Emoji emoji);
	}



	private final int childViewHeight;
	private final int childViewWidth;
	private final int emojiSize;
	private List<Emoji> emojiList;
	private final OnEmojiClickListener listener;

	public HSEmojiSkinViewAdapter(int childViewHeight, int childViewWidth, float scaleRatio, OnEmojiClickListener listener) {
		this.childViewHeight = childViewHeight;
		this.childViewWidth = childViewWidth;
		emojiSize = (int) (Math.min(childViewWidth,childViewHeight) * scaleRatio);
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_skin_view,parent,false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if(emojiList == null){
			return;
		}
		final Emoji emoji = emojiList.get(position);
		Button textView = holder.tv;

		textView.setText(emoji.getLabel());
		if(!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()){
			textView.setTextColor(Color.BLACK);
		}
		if(emoji.isText()){
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, emojiSize * 0.9f);
		}else{
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, emojiSize);
		}
		textView.setHorizontallyScrolling(false);//must

		RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
		lp.width = childViewWidth * emoji.getColumn();
		lp.height = childViewHeight;

		if(emoji.isDivider()){
			lp.width = HSDisplayUtils.dip2px(20);
		}
		holder.itemView.setLayoutParams(lp);

		if(emoji.getLabel().trim().length()>0){
			textView.setClickable(true);
			textView.setTag(emoji);
			textView.setOnClickListener(this);
			textView.setSoundEffectsEnabled(false);
			holder.itemView.setOnClickListener(this);
			holder.itemView.setTag(emoji);
		}else{
			holder.itemView.setOnClickListener(null);
			holder.itemView.setTag(null);
			textView.setTag(null);
			textView.setClickable(false);
		}

	}

	@Override
	public int getItemCount() {
		if(emojiList != null){
			return emojiList.size();
		}
		return 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setData(List<Emoji> emojis) {
		this.emojiList = emojis;
		notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		final Object tag = v.getTag();
		if(tag instanceof Emoji && listener!=null){
			listener.onEmojiSkinClick((Emoji) tag);

			Animation set = createClickAnimation(1.4f,80,80);

			v.startAnimation(set);
		}
	}

	private Animation createClickAnimation(final float scaleRation, final int upDuration, final int downDuration){

		final Animation scaleUp = new ScaleAnimation(
				1.0f,scaleRation,1.0f,scaleRation,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
		);
		final Animation scaleDown = new ScaleAnimation(
				1.0f,1/scaleRation,1.0f,1/scaleRation,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
		);
		final AnimationSet set = new AnimationSet(false);
		scaleUp.setDuration(upDuration);
		scaleUp.setFillAfter(true);
		scaleDown.setDuration(downDuration);
		scaleDown.setStartOffset(upDuration);
		scaleDown.setFillAfter(true);
		set.setDuration(upDuration+downDuration);

		set.addAnimation(scaleUp);
		set.addAnimation(scaleDown);

		return set;
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		Button tv;
		public ViewHolder(View itemView) {
			super(itemView);
			tv = itemView.findViewById(R.id.emoji_skin_tv);

		}
	}
}
