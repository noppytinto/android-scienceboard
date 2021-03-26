package com.nocorp.scienceboard.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nocorp.scienceboard.R;

public class ArticleImageGetter implements Html.ImageGetter {
    @Override
    public Drawable getDrawable(String source) {
        return null;
    }



    public static void downloadDrawableFromUrl(String imageUrl, MenuItem menuItem, Context context) {
        try {
            Glide.with(context)  //2
                    .asDrawable()
                    .load(imageUrl) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            try {
                                Drawable drawable = resource;
                                menuItem.setIcon(drawable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    }); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class DrawablePlaceholder extends Drawable{

        @Override
        public void draw(@NonNull Canvas canvas) {

        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }
}
