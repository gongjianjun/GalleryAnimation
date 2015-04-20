package io.github.francisshi.galleryanimation.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

import java.util.ArrayList;

/**
 * Created by Francis on 15/4/17.
 */
public class Util {

    public static ArrayList<Integer> getImageRes(Context context) {
        ArrayList<Integer> imageRes = new ArrayList<>();
        int returnSize = 9;
        for (int i = 0; i < returnSize; i++) {
            int res = context.getResources().getIdentifier("d" + (i % 11 + 1), "drawable", context.getPackageName());
            if (res != 0)
                imageRes.add(res);
        }
        return imageRes;
    }

    /**
     * 获取屏幕顶部StatusBar的高度
     *
     * @param currentActivity
     * @return
     */
    public static int getStatusBarHeight(Activity currentActivity) {
        int result = 0;
        int resourceId = currentActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = currentActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static float convertPixelsToDp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public static Point getFitSize(final int width, final int height, final int containerWidth, final int containerHeight) {
        int resultHeight, resultWidth;
        resultHeight = height * containerWidth / width;
        if (resultHeight <= containerHeight) {
            resultWidth = containerWidth;
        } else {
            resultWidth = width * containerHeight / height;
            resultHeight = containerHeight;
        }
        return new Point(resultWidth, resultHeight);
    }
}