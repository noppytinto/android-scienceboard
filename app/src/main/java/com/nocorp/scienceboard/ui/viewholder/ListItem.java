package com.nocorp.scienceboard.ui.viewholder;

import com.nocorp.scienceboard.utility.MyValues.ItemType;

public abstract class ListItem {
    private ItemType itemType;

    public ListItem(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
