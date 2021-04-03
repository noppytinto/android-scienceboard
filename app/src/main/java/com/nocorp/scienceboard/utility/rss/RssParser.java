package com.nocorp.scienceboard.utility.rss;

import com.nocorp.scienceboard.model.Source;

public interface RssParser {
    public Source getSource(String url);
}
