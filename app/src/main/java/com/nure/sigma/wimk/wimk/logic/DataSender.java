package com.nure.sigma.wimk.wimk.logic;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

/**
 * Created by andstepko on 31.10.15.
 */
public class DataSender {
    public DataSender() {
    }

    public MyHttpResponse HttpPostQuery(String serverUrl, List<Pair<String, String>> pairs,
                                        int waitResponseTimeout){
        try {
            URL obj = null;
            try {
                obj = new URL(serverUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.BAD_URL, null);
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.OPEN_CONNECTION_FAIL, null);
            }

            try {
                connection.setRequestMethod("POST");
            } catch (java.net.ProtocolException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.SET_REQUEST_METHOD_FAIL, null);
            }

            StringBuilder urlParameters = new StringBuilder();

            for (Pair<String, String> pair : pairs) {
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
                return new MyHttpResponse(MyHttpResponse.OUTPUT_STREAM_FAIL, null);
            }

            String response = null;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                response = String.valueOf(sb.toString());

                if (response == null) {
                    return new MyHttpResponse(MyHttpResponse.RESPONSE_NULL, null);
                }

                return new MyHttpResponse(MyHttpResponse.OK, response.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.GET_RESPONSE_FAIL, null);
            }
        }
        catch (Exception e){
            return new MyHttpResponse(MyHttpResponse.UNKNOWN_ERROR, null);
        }
    }

    public MyHttpResponse HttpGetQuery(String serverUrl, int waitResponseTimeout){
        try{
            URL obj = null;
            try {
                obj = new URL(serverUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.BAD_URL, null);
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.OPEN_CONNECTION_FAIL, null);
            }

            if(connection == null){
                return new MyHttpResponse(MyHttpResponse.NULL_CONNECTION, null);
            }

            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.SET_REQUEST_METHOD_FAIL, null);
            }

            int responseCode = connection.getResponseCode();

            StringBuilder response = null;

            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            }
            catch (IOException e){
                e.printStackTrace();
                return new MyHttpResponse(MyHttpResponse.GET_RESPONSE_FAIL, null);
            }

            try{
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufReader = new BufferedReader(inputStreamReader);

                String inputLine;
                response = new StringBuilder();

                while ((inputLine = bufReader.readLine()) != null) {
                    response.append(inputLine);
                }
                bufReader.close();
            }
            catch (IOException e){
                return new MyHttpResponse(MyHttpResponse.OUTPUT_STREAM_FAIL, null);
            }

            if(response == null){
                return new MyHttpResponse(MyHttpResponse.RESPONSE_NULL, null);
            }

            return new MyHttpResponse(MyHttpResponse.OK, response.toString());
        }
        catch (Exception e){
            return new MyHttpResponse(MyHttpResponse.UNKNOWN_ERROR, null);
        }
    }
}
