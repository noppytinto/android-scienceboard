package com.nocorp.scienceboard.topics.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Topic {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(defaultValue = "true")
    private boolean followed;

    @ColumnInfo(name = "display_name")
    private String displayName;


    public Topic() {
    }

    @Ignore
    public Topic(@NonNull String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        followed = true;
    }

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

}// end Topic
