package com.nocorp.scienceboard.utility;

import android.util.Patterns;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpUtilitiesTest {

//    @Mock
//    HttpUtilities mockHttpUtilities;

//    @Before
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//    }








    //------------------------------------------------------------------------- sanitizeUrl(String)

    //----------------- assertions

    @Test(expected = URISyntaxException.class)
    public void sanitizeUrl_given_nullUrl_thenThrow_URISyntaxException() throws URISyntaxException {
        String urlString = null;
        HttpUtilities.sanitizeUrl(urlString);
    }

    @Test(expected = URISyntaxException.class)
    public void sanitizeUrl_given_emptyUrl_thenThrow_URISyntaxException() throws URISyntaxException {
        String urlString = "";
        HttpUtilities.sanitizeUrl(urlString);
    }

    @Test(expected = URISyntaxException.class)
    public void sanitizeUrl_given_blankUrl_thenThrow_URISyntaxException() throws URISyntaxException {
        String urlString = "   ";
        HttpUtilities.sanitizeUrl(urlString);
    }

    @Test(expected = URISyntaxException.class)
    public void sanitizeUrl_given_validUrlWithDifferentprotocol_thenThrow_URISyntaxException() throws URISyntaxException {
        String testingValue = "ftp://www.google.it";
        HttpUtilities.sanitizeUrl(testingValue);
    }

    @Test
    public void sanitizeUrl_given_validUrlWithBlanksAndHTTP_thenReturn_sanitizedString() throws URISyntaxException {
        String testingValue = "   http://www.google.it ";
        String expected = "http://www.google.it";
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }

    @Test
    public void sanitizeUrl_given_validUrlWithBlanksAndHTTPS_thenReturn_sanitizedString() throws URISyntaxException {
        String testingValue = "   https://www.google.it ";
        String expected = "https://www.google.it";
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }

    @Test
    public void sanitizeUrl_given_validUrlWithHTTPprotocol_thenReturn_sanitizedString() throws URISyntaxException {
        String testingValue = "http://www.google.it";
        String expected = "http://www.google.it";
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }

    @Test
    public void sanitizeUrl_given_validUrlWithHTTPSprotocol_thenReturn_sanitizedString() throws URISyntaxException {
        String testingValue = "   https://www.google.it ";
        String expected = "https://www.google.it";
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }

    @Test
    public void sanitizeUrl_given_validUrlWithSubpaths_thenReturn_sanitizedString() throws URISyntaxException {
        String testingValue = "http://www.google.it/subpath1/subpath2";
        String expected = testingValue;
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }

    @Test
    public void sanitizeUrl_given_validUrlWithSubpathsAndQuery_thenReturn_sanitizedStringWithoutQuery() throws URISyntaxException {
        String testingValue = "http://www.google.it/subpath1/subpath2?query=test";
        String expected = "http://www.google.it/subpath1/subpath2";
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }


    @Test
    public void sanitizeUrl_given_validUrlWithSubpathsAndFile_thenReturn_sanitizedStringWithFile() throws URISyntaxException {
        String testingValue = "https://home.cern/api/news/news/feed.rss";
        String expected = testingValue;
        assertEquals(expected, HttpUtilities.sanitizeUrl(testingValue));
    }






    //------------------------------------------------------------------------- isValidUrl(String)


    @Test
    public void isValidUrl_givenNullUrlString_thenReturnsFalse() {
        String urlString = null;
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenEmptyUrlString_thenReturnsFalse() {
        String urlString = "";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenEmptyUrlStringWithBlanks_thenReturnsFalse() {
        String urlString = "   ";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }


    @Test
    public void isValidUrl_givenInvalidUrlString_1_thenReturnsFalse() {
        String urlString = "https://www.google.com////";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenInvalidUrlString_2_thenReturnsFalse() {
        String urlString = "https://www..google.com////";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenInvalidUrlString_3_thenReturnsFalse() {
        String urlString = "erherle.com////";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithFtpProtocol_thenReturnsFalse() {
        String urlString = "ftp://www.google.com////";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithoutProtocol_thenReturFalse() {
        String urlString = "www.google.com";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithIp_thenReturFalse() {
        String urlString = "http://192.168.1.1";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithBlanksWithin_thenReturnsFalse() {
        String urlString = "https://www      .google.com";
        assertFalse(HttpUtilities.isValidUrl(urlString));
    }



    //----------------- true assertions

    @Test
    public void isValidUrl_givenValidUrlStringWithoutWWW_thenReturTrue() {
        String urlString = "http://google.com";
        assertTrue(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithHttpProtocol_thenReturnsTrue() {
        String urlString = "http://www.google.com";
        assertTrue(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithHttpsProtocol_thenReturnsTrue() {
        String urlString = "https://www.google.com";
        assertTrue(HttpUtilities.isValidUrl(urlString));
    }

    @Test
    public void isValidUrl_givenValidUrlStringWithBlanks_thenReturnsTrue() {
        String urlString = "         https://www.google.com        ";
        assertTrue(HttpUtilities.isValidUrl(urlString));
    }






    // ---------------------- investigate

    // should return false
    @Test
    public void isValidUrl_givenValidUrlStringWithoutDotCom_thenReturnTrue() {
        String urlString = "http://www.google";
        assertTrue(HttpUtilities.isValidUrl(urlString));
    }




}
