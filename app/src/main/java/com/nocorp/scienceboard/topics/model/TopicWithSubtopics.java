package com.nocorp.scienceboard.topics.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TopicWithSubtopics {
    @Embedded
    public Topic parentTopic;
    @Relation(
            parentColumn = "id",
            entityColumn = "id"
    )
    public List<Topic> subtopics;

}
