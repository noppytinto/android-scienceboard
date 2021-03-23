package com.nocorp.scienceboard.utility;

import android.util.Patterns;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpUtilitiesTest {

//    @Mock
//    HttpUtilities mockHttpUtilities;

//    @Before
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//    }

    @Test
    public void isValidUrl_givenEmptyNullUrlString_thenReturnsFalse() {
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
