package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.List;

public class MyTopics extends ListItem {
    private List<ListItem> myTopics;
    public MyTopics() {
        setItemType(MyValues.ItemType.MY_TOPICS_LIST);
    }

    public MyTopics(List<ListItem> topics) {
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
