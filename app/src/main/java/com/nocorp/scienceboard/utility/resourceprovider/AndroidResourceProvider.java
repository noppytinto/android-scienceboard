package com.nocorp.scienceboard.utility.resourceprovider;

import android.content.Context;

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
}
