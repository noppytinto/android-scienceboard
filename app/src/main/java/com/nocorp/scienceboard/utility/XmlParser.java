package com.nocorp.scienceboard.utility;

import com.nocorp.scienceboard.model.Channel;

import org.w3c.dom.Element;

import java.io.InputStream;

public interface XmlParser {
    public Channel getChannelInfo(String input);

}
