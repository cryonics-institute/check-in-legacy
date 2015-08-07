package com.cryonicsinstitute.util;

import android.content.Context;

public class Util {
    private static float   DIP_SCALE = -1;

    public static int dpToPx(int dp, Context context) {
        if(DIP_SCALE == -1) DIP_SCALE = context.getResources().getDisplayMetrics().density;
        return (int)(dp * DIP_SCALE + 0.5f); // 0.5f for rounding
    }
    public static int pxToDp(int px, Context context) {
        if(DIP_SCALE == -1) DIP_SCALE = context.getResources().getDisplayMetrics().density;
        return (int)(px / DIP_SCALE + 0.5f); // 0.5f for rounding
    }
}
