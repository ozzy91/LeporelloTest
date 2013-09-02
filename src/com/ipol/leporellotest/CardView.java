package com.ipol.leporellotest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public class CardView extends RelativeLayout {
	private View viwBorder;
	private CardView viwSecondCard;
	private int initTranslationX;
	private int lastTranslationX;
	private float lastScaleY;
	
	public CardView(Context context) {
		super(context);
	}

	@Override
	public void setTranslationX(float translationX) {
		super.setTranslationX(translationX);
		if (viwBorder != null)
			viwBorder.setTranslationX(getTranslationX() + getLayoutParams().width);
		if (viwSecondCard != null) {
			viwSecondCard.setTranslationX(getTranslationX());
		}
		
		// Pos of end of card: ca. 404
		// degree: 45
		// pos of start of card: 645px (length) + 15px (initTranslation) = 660
		// diff on z = 0: 256px
	}

	public void setBorderRight(View viwBorder) {
		this.viwBorder = viwBorder;
		viwBorder.setTranslationX(getTranslationX() + getLayoutParams().width);
	}
	
	public View getBorderRight() {
		return viwBorder;
	}
	
	public void setInitialTranslationX(int initialTranslationX) {
		this.initTranslationX = initialTranslationX;
		lastTranslationX = initialTranslationX;
		setTranslationX(initialTranslationX);
	}
	
	public int getInitialTranslationX() {
		return initTranslationX;
	}
	
	public void setLastTranslationX(int lastTranslationX) {
		this.lastTranslationX = lastTranslationX;
	}
	
	public int getLastTranslationX() {
		return lastTranslationX;
	}

	public float getLastScaleY() {
		return lastScaleY;
	}

	public void setLastScaleY(float lastScaleY) {
		this.lastScaleY = lastScaleY;
	}
}
