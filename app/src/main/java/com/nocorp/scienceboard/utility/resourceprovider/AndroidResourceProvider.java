package com.nocorp.scienceboard.utility.resourceprovider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.res.ResourcesCompat;


public class AndroidResourceProvider implements ResourceProvider {
    private Context context;

    public AndroidResourceProvider(Context context) {
        this.context = context;
    }

    @Override
    public String getString(int resourceId) {
        return context.getString(resourceId);
    }

    @Override
    public int getColor(int resourceId) {
        return context.getResources().getColor(resourceId);
    }

    @Override
    public Drawable getDrawable(int resourceId) {
        return ResourcesCompat.getDrawable(context.getResources(), resourceId, null);
    }
}// end AndroidResourceProvider
