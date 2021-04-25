package com.nocorp.scienceboard.ui.viewholder;

import androidx.room.Ignore;

import com.nocorp.scienceboard.utility.MyValues.ItemType;

public abstract class ListItem {
    @Ignore
    private ItemType itemType;
    private boolean isSelected;
    private boolean isEditable;

    public ListItem() {
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }
}
