package com.nocorp.scienceboard.utility;

import android.util.Patterns;

import org.apache.commons.validator.routines.UrlValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtilities {
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";

    public static String sanitizeUrl(String urlString) throws URISyntaxException {
        if( ! isValidUrl(urlString)) return null;

        URI uri = new URI(urlString);
        String protocol = uri.getScheme();
        if( ! isValidProtocol(protocol)) return null;
        String domain = uri.getHost();
        String path = uri.getPath();

//        return buildSafeUrl(domain);
        return uri.toString();
    }

    public static boolean isValidProtocol(String protocol) {
        if(protocol.equals(HTTP_PROTOCOL)) {
            return true;
        }
        else if(protocol.equals(HTTPS_PROTOCOL)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * - The URL must start with either http or https and
     * - then followed by :// and
     * - then it must contain www. and
     * - then followed by subdomain of length (2, 256) and
     * - last part contains top level domain like .com, .org etc.
     */
    public static boolean isValidUrl(String urlString) {
        if(urlString==null) return false;
        String trimmedString = urlString.trim();
        if(trimmedString.isEmpty()) return false;

        return checkUrlFormatWithUrlValidator(trimmedString)
                && checkUrlFormatWithRegex(trimmedString);
    }

    private static boolean checkUrlFormatWithRegex(String urlString) {
        String regex = "((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]"
                + "{2,256}\\.[a-z]"
                + "{2,6}\\b([-a-zA-Z0-9@:%"
                + "._\\+~#?&//=]*)";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(urlString);
        return m.matches();
    }

    private static boolean checkUrlFormatWithUrlValidator(String urlString) {
        String[] customSchemes = { "http", "https" };
        UrlValidator urlValidator = new UrlValidator(customSchemes);
        return urlValidator.isValid(urlString);
    }

    // TODO: unit tests return nullpointer, maybe is an integration test?
    private static boolean checkUrlFormatWithAndroidPatternsClass(String urlString) {
        return Patterns.WEB_URL.matcher(urlString).matches();
    }

    public static String buildSafeUrl(String host) {
        return HTTPS_PROTOCOL + "://" + host;
    }

    public static String buildUnsafeUrl(String host) {
        return HTTP_PROTOCOL + "://" + host;
    }



//    public static String buildUrl(String protocol, String host) {
//        String finaUrl = null;
//
//        if(isValidProtocol(protocol) && isValidHost(host)) {
//        }
//        return protocol + host;
//    }



}// end HttpUtilities
