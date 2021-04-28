package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.List;

public class MyTopics extends ListItem {
    private List<Topic> myTopics;
    public MyTopics() {
        setItemType(MyValues.ItemType.MY_TOPICS_LIST);
    }

    public MyTopics(List<Topic> topics) {
        setItemType(MyValues.ItemType.MY_TOPICS_LIST);
        this.myTopics = topics;
    }

    public List<Topic> getMyTopics() {
        return myTopics;
    }

    public void setMyTopics(List<Topic> myTopics) {
        this.myTopics = myTopics;
    }
}
