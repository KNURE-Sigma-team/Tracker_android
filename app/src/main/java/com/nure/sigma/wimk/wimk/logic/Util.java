package com.nure.sigma.wimk.wimk.logic;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created by andrew on 17.10.15.
 */
public class Util {

    public static final int OK = 0;
    public static final int BAD_URL = 101;
    public static final int OPEN_CONNECTION_FAIL = 102;
    public static final int NULL_CONNECTION = 103;
    public static final int SET_POST_FAIL = 104;
    public static final int OUTPUT_STREAM_FAIL = 105;
    public static final int GET_RESPONSE_FAIL = 106;

    public static int HttpPostRequest(String serverUrl, Collection<Pair<String, String>> pairs){
        URL obj = null;
        try {
            obj = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(connection == null){
        }

        try {
            connection.setRequestMethod("POST");
        } catch (java.net.ProtocolException e) {
            e.printStackTrace();
        }

        StringBuilder urlParameters = new StringBuilder();

        for(Pair<String, String> pair : pairs){
            urlParameters.append(pair.first);
            urlParameters.append("=");
            urlParameters.append(pair.second);
            urlParameters.append("&");
        }

        // Delete last ampersand
        urlParameters.delete(urlParameters.length() - 1, urlParameters.length());

        // Send post request
        connection.setDoOutput(true);

        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters.toString());
            wr.flush();
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }
            response = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.getInteger(response);
    }


}
