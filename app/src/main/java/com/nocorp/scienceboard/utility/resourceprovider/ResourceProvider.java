package com.nocorp.scienceboard.utility.resourceprovider;

import android.graphics.drawable.Drawable;

public interface ResourceProvider {
    public String getString(int resourceId);
    public int getColor(int resourceId);
    public Drawable getDrawable(int resourceId);
}// end ResourceProvider
