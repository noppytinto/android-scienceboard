package com.nocorp.scienceboard.utility.rss;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;

import java.util.List;

public interface RssParser {
    Source downloadSourceData(String rssUrl);
    List<Article> downloadArticles(Source source, int limit);
    Source updateSource(Source source);
}
