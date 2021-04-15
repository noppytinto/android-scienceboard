package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

public class LoadingViewItem extends ListItem {
    public LoadingViewItem() {
        setItemType(MyValues.ItemType.LOADING_VIEW);
    }

}
