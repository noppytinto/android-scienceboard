package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.List;

public class MyTopicsItem extends ListItem {
    private List<ListItem> myTopics;
    public MyTopicsItem() {
        setItemType(MyValues.ItemType.MY_TOPICS_LIST);
    }

    public MyTopicsItem(List<ListItem> topics) {
        setItemType(MyValues.ItemType.MY_TOPICS_LIST);
        this.myTopics = topics;
    }

    public List<ListItem> getMyTopics() {
        return myTopics;
    }

    public void setMyTopics(List<ListItem> myTopics) {
        this.myTopics = myTopics;
    }
}
