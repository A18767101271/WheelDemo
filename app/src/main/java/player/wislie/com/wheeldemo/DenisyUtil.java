package player.wislie.com.wheeldemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DenisyUtil {

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        return width;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;       // 屏幕高度（像素）
        return height;
    }

    public static int getWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        return outSize.x;
    }

    public static int getHeight(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        return outSize.y;
    }

    /**
     * dp转换成px
     */
    public static float dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    /**
     * px转换成dp
     */
    public static float px2dp(Context context, float pxValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    /**
     * sp转换成px
     */
    public static float sp2px(Context context, float spValue){
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    /**
     * px转换成sp
     */
    public static float px2sp(Context context, float pxValue){
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale;
    }
}
