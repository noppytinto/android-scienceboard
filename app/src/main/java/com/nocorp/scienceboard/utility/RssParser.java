package com.nocorp.scienceboard.utility;

import com.nocorp.scienceboard.model.Source;

public interface RssParser {
    public Source getSource(String url);
}
