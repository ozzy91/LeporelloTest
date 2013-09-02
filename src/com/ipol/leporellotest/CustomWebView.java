package com.ipol.leporellotest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class CustomWebView extends WebView {

	public CustomWebView(Context context) {
		super(context);
		
		initSettings();
		initLayout(context);
	}
	
	public void initSettings() {
		setBackgroundColor(Color.BLACK);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		setScrollbarFadingEnabled(false);
		setHapticFeedbackEnabled(false);
		setFocusableInTouchMode(false);
		setFocusable(false);
		
		getSettings().setLightTouchEnabled(false);
		getSettings().setRenderPriority(RenderPriority.HIGH);
		getSettings().setJavaScriptEnabled(true);
		getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public void initLayout(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		int displayWidth = display.getWidth();
		
		float scale_320;
		scale_320 = (((float) displayWidth / 320) * 100);
		scale_320 = (int) Math.floor(scale_320);
		setInitialScale((int) scale_320);	
	}
}
