package com.nocorp.scienceboard.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;

public class TopicWithSubtopics {
    @Embedded
    public Topic parentTopic;
    @Relation(
            parentColumn = "name",
            entityColumn = "name"
    )
    public List<Topic> subtopics;

}
