package com.nocorp.scienceboard.utility.ad.admob.model;

//import com.google.android.gms.ads.nativead.NativeAd;
import com.appodeal.ads.NativeAd;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues.ItemType;

public class ListAd extends ListItem {
    private NativeAd ad;

    public ListAd() {
        setItemType(ItemType.SMALL_AD);
    }

    public NativeAd getAd() {
        return ad;
    }

    public void setAd(NativeAd ad) {
        this.ad = ad;
    }
}
