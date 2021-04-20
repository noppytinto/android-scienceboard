package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

@Entity
public class Topic {
    @PrimaryKey
    @NonNull
    private String name;

    @ColumnInfo(defaultValue = "true")
    private boolean followed;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
