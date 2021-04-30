package com.nocorp.scienceboard.topics.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.io.Serializable;

@Entity
public class Topic extends ListItem implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(defaultValue = "true")
    private boolean followed;

    @ColumnInfo(name = "display_name")
    private String displayName;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;




    //------------------------------------------------------------ CONSTRUCTORS

    public Topic() {
        setItemType(MyValues.ItemType.TOPIC);
    }

    @Ignore
    public Topic(@NonNull String id, String displayName) {
        setItemType(MyValues.ItemType.TOPIC);
        this.id = id;
        this.displayName = displayName;
        followed = true;
    }








    //------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public List<Topic> getSubtopics() {
//        return subtopics;
//    }
//
//    public void setSubtopics(List<Topic> subtopics) {
//        this.subtopics = subtopics;
//    }

    public boolean getFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

//    public List<Source> getSources() {
//        return sources;
//    }
//
//    public void setSources(List<Source> sources) {
//        this.sources = sources;
//    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}// end Topic
