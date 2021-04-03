package com.nocorp.scienceboard.utility.rss;

import android.content.Context;

import com.nocorp.scienceboard.utility.rss.model.Channel;

public interface XmlParser {
    public Channel getChannel(String rssUrl, Context context);

}
