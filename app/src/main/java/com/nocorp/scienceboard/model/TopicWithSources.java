package com.nocorp.scienceboard.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;

public class TopicWithSources {
    @Embedded
    public Topic parentTopic;
    @Relation(
            parentColumn = "name",
            entityColumn = "id"
    )
    public List<Source> sources;
}
