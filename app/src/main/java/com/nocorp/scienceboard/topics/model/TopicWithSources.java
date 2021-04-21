package com.nocorp.scienceboard.topics.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.nocorp.scienceboard.model.Source;

import java.util.List;

public class TopicWithSources {
    @Embedded
    public Topic parentTopic;
    @Relation(
            parentColumn = "id",
            entityColumn = "id"
    )
    public List<Source> sources;
}
