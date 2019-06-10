package com.example.hi.imageloaderchooseimage.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * Created by hi on 2019.6.9.
 */

public class ViewSize {

    private View view;
    private SizeLayoutListener layoutListener;
    private int height=0;
    private int width=0;

    public ViewSize(View view) {
        this.view = view;
        if (view!=null){
            init();
        }
    }

    private void init() {
        getViewHeight();
        getViewWidth();

    }


    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    private void checkCurrentDimens(int currentWidth, int currentHeight) {
        if (currentWidth>0&&currentWidth<width){
            width=currentWidth;
        }
        if (currentHeight>0&&currentHeight<height){
            height=currentHeight;
        }
    }

    private  void getViewWidth() {
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (isSizeValid(view.getWidth())) {
             width=view.getWidth();
        } else if (layoutParams != null) {
            width=getSizeForParam(layoutParams.width, false /*isHeight*/, view);
        } else {
            width=0;
        }
    }

    private   void getViewHeight() {
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (isSizeValid(view.getHeight())) {
            height=view.getHeight();
        } else if (layoutParams != null) {
            height=getSizeForParam(layoutParams.height, true /*isHeight*/,view);
        } else {
            height=0;
        }
    }

    private static boolean isSizeValid(int size) {
        return size > 0 || size == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private static int getSizeForParam(int param, boolean isHeight, View view) {
        if (param == ViewGroup.LayoutParams.WRAP_CONTENT) {
            Point displayDimens = getDisplayDimens(view);
            return isHeight ? displayDimens.y : displayDimens.x;
        } else {
            return param;
        }
    }

    private static Point getDisplayDimens(View view) {
        Point displayDimens;
        WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            displayDimens = new Point();
            display.getSize(displayDimens);
        } else {
            displayDimens = new Point(display.getWidth(), display.getHeight());
        }
        return displayDimens;
    }



    private static class SizeLayoutListener implements ViewTreeObserver.OnPreDrawListener {
        private final WeakReference<ViewSize> viewSizeListener;

        public SizeLayoutListener(ViewSize view) {
            viewSizeListener = new WeakReference<>(view);
        }

        @Override
        public boolean onPreDraw() {

            ViewSize viewSize = viewSizeListener.get();
            if (viewSize != null) {
                View view=viewSize.getView();
                int currentWidth = view.getWidth() ;
                int currentHeight = view.getHeight();
                viewSize.checkCurrentDimens(currentWidth,currentHeight);
            }
            return true;
        }
    }
}
