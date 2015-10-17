package com.nure.sigma.wimk.wimk.logic;

import android.util.Pair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created by andrew on 17.10.15.
 */
public class DataSender {

    public static final int OK = 0;
    public static final int BAD_URL = 101;
    public static final int OPEN_CONNECTION_FAIL = 102;
    public static final int NULL_CONNECTION = 103;
    public static final int SET_POST_FAIL = 104;
    public static final int OUTPUT_STREAM_FAIL = 105;
    public static final int GET_RESPONSE_FAIL = 106;

    private static DataSender ourInstance = new DataSender();

    public static DataSender getInstance() {
        return ourInstance;
    }

    private DataSender() {
    }

    public int HttpPostQuery(String serverUrl, Collection<Pair<String, String>> pairs){
        URL obj = null;
        try {
            obj = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return BAD_URL;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return OPEN_CONNECTION_FAIL;
        }

        if(connection == null){
            return NULL_CONNECTION;
        }

        try {
            connection.setRequestMethod("POST");
        } catch (java.net.ProtocolException e) {
            e.printStackTrace();
            return SET_POST_FAIL;
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
            return OUTPUT_STREAM_FAIL;
        }

        String response = null;

        try {
            response = connection.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return GET_RESPONSE_FAIL;
        }
        return OK;
    }


}
