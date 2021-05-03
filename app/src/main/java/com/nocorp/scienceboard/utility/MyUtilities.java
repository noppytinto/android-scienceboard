package com.nocorp.scienceboard.utility;

import android.content.Context;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MyUtilities {

    /**
     * Return date in specified format.
     * @param millis Date in milliseconds
     * @return String representing date in specified format
     */
    public static String convertMillisInDate(long millis)
    {
        // date format examples
        // System.out.println(getDate(82233213123L, "dd/MM/yyyy hh:mm:ss.SSS"));

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return formatter.format(calendar.getTime());
    }

    public static long convertStringDateInMillis(String myDate) {
        // NOTE: from epoch
        // f.e. :
        // String myDate = "1990/01/01 00:00:00";
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // String myDate = "1990/01/01";
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        Date date = null;
        long result = (long) 0.0;
        try {
            date = sdf.parse(myDate);
            result = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String convertMillisToReadableTimespan(long millis) {
        if(millis<0) return "";

        String res = "";
        long currentTimeMillis = System.currentTimeMillis();
        long timespan = currentTimeMillis-millis;

        long seconds = (TimeUnit.MILLISECONDS.toSeconds(timespan));

        if(seconds>=60) {
            long minutes = (TimeUnit.MILLISECONDS.toMinutes(timespan));
            if(minutes>=60) {
                long hours = (TimeUnit.MILLISECONDS.toHours(timespan));
                if(hours>=24) {
                    long days = (TimeUnit.MILLISECONDS.toDays(timespan));

                    if(days==1)
                        res = days + " d";
                    else
                        res = days + " d";
                }
                else {
                    if(hours==1)
                        res = hours + " h";
                    else
                        res = hours + " h";
                }
            }
            else {
                if(minutes==1)
                    res = minutes + " m";
                else
                    res = minutes + " m";
            }
        }
        else res = " s";

        return res;
    }


    public static boolean isWithin_seconds(int givenSeconds, long startingMillis) {
        boolean result = true;
        if(givenSeconds<=0 || startingMillis<=0) return false;

        // get current time
        long currentMillis = System.currentTimeMillis();
        if(startingMillis>currentMillis) return result;

        // calculate diff
        long diffInMillis = currentMillis - startingMillis;
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);

        //
        result = diffInSeconds >= 0 && diffInSeconds <= givenSeconds;

        Log.d("MyUtilities: ", "isWithin_seconds: seconds: " + givenSeconds + ", result: " + result);

        return result;
    }

    public static boolean isWithin_minutes(int givenMinutes, long startingMillis) {
        boolean result = true;
        if(givenMinutes<=0 || startingMillis<=0) return false;

        // get current time
        long currentMillis = System.currentTimeMillis();
        if(startingMillis>currentMillis) return result;

        // calculate diff
        long diffInMillis = currentMillis - startingMillis;
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

        //
        result = diffInMinutes >= 0 && diffInMinutes <= givenMinutes;

        Log.d("MyUtilities: ", "isWithin_minutes: minutes: " + givenMinutes + ", result: " + result);

        return result;
    }

    public static boolean isWithin_hours(int givenHours, long startingMillis) {
        boolean result = true;
        if(givenHours<=0 || startingMillis<=0) return false;

        // get current time
        long currentMillis = System.currentTimeMillis();
        if(startingMillis>currentMillis) return result;

        // calculate diff
        long diffInMillis = currentMillis - startingMillis;
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

        //
        result = diffInHours >= 0 && diffInHours <= givenHours;

        Log.d("MyUtilities: ", "isWithin_hours: hours: " + givenHours + ", result: " + result);

        return result;
    }

    public static boolean isWithin_days(int givenDays, long startingMillis) {
        boolean result = true;
        if(givenDays<=0 || startingMillis<=0) return false;

        // get current time
        long currentMillis = System.currentTimeMillis();
        if(startingMillis>currentMillis) return result;

        // calculate diff
        long diffInMillis = currentMillis - startingMillis;
        long diffInHours = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        //
        result = diffInHours >= 0 && diffInHours <= givenDays;

        Log.d("MyUtilities: ", "isWithin_days: days: " + givenDays + ", result: " + result);

        return result;
    }

//    public static void encryptFile(String filename, String rawData, Context context) {
//        try {
//            MasterKey masterKey = new MasterKey.Builder(context)
//                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//                    .build();
//
//            // Creates a file with this name, or replaces an existing file
//            // that has the same name. Note that the file name cannot contain
//            // path separators.
//            File fileToWrite = new File(context.getFilesDir(), filename);
//            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
//                    fileToWrite,
//                    masterKey,
//                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
//            ).build();
//
//            // File cannot exist before using openFileOutput
//            if (fileToWrite.exists()) {
//                fileToWrite.delete();
//            }
//
//            byte[] fileContent = rawData.getBytes(StandardCharsets.UTF_8);
//            OutputStream outputStream = encryptedFile.openFileOutput();
//            outputStream.write(fileContent);
//            outputStream.flush();
//            outputStream.close();
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String decryptFile(String filename, Context context) {
//        String result=null;
//
//        try {
//            MasterKey masterKey = new MasterKey.Builder(context)
//                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//                    .build();
//
//            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
//                    new File(context.getFilesDir(), filename),
//                    masterKey,
//                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
//            ).build();
//
//            InputStream inputStream = encryptedFile.openFileInput();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            int nextByte = inputStream.read();
//            while (nextByte != -1) {
//                byteArrayOutputStream.write(nextByte);
//                nextByte = inputStream.read();
//            }
//
//            result = byteArrayOutputStream.toString();
//
//        } catch (IOException | GeneralSecurityException e) {
//            e.printStackTrace();
//        }
//
//
//        return result;
//    }

    public static boolean checkFileExists(String filename, Context context) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }

    public static boolean deletFile(String filename, Context context) {
        boolean result = false;
        File file = new File(context.getFilesDir(), filename);
        if(file.exists()) {
            file.delete();
            result  = true;
        }

        return result;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static String SHA256encrypt(String message) {
        if(message== null || message.isEmpty()) return null;

        String hex = null;

        try {
            hex = toHexString(getSHA(message));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return hex;
    }


    public static String convertStringDateToStringSqlDate(String date) {
        if(date==null || date.isEmpty()) return null;

        Date parsed = null;
        SimpleDateFormat format = null;
        String sqldate = null;
        try {
            format = new SimpleDateFormat("dd-MM-yyyy");
            parsed = format.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                parsed = format.parse(date);
            } catch (ParseException parseException) {
                parseException.printStackTrace();

            }
        }
        java.sql.Date sqlStartDate = new java.sql.Date(parsed.getTime());
        sqldate = String.valueOf(sqlStartDate);

        return sqldate;
    }

    public static java.sql.Date convertStringDateToSqlDate(String date) {
        if(date==null || date.isEmpty()) return null;

        Date parsed = null;
        SimpleDateFormat format = null;
        java.sql.Date sqldate = null;
        try {
            format = new SimpleDateFormat("dd-MM-yyyy");
            parsed = format.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                parsed = format.parse(date);
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
        sqldate = new java.sql.Date(parsed.getTime());

        return sqldate;
    }

}// end MyUtilities class
