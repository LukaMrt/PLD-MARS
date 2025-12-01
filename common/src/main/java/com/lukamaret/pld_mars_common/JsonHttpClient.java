package com.lukamaret.pld_mars_common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.lukamaret.pld_mars_common.exception.ServiceIOException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author WASO Team
 */
public class JsonHttpClient {

    public static class Parameter extends BasicNameValuePair {

        public Parameter(String name, String value) {
            super(name, value);
        }
    }

    protected CloseableHttpClient httpclient;

    public JsonHttpClient() {
        httpclient = HttpClients.createDefault();
    }

    public void close() throws IOException {
        httpclient.close();
    }

    public JsonObject post(String url, NameValuePair... parameters) throws IOException {

        JsonElement responseElement = null;
        JsonObject responseContainer = null;
        int responseStatus;

        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(parameters), StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                responseStatus = response.getCode();
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                        responseElement = JsonParser.parseReader(jsonReader);
                    }
                }
            }

            if (responseStatus == 200 && responseElement != null) {
                responseContainer = responseElement.getAsJsonObject();
            }

        } catch (HttpHostConnectException ex) {
            throw new ServiceIOException("Service Request FAILED: Could NOT CONNECT to remote Server ~~> check target URL ???" + "\n******** Target URL =>  " + url + "  <= ********"+"\n");
        } catch (IllegalStateException ex) {
            throw new ServiceIOException("Service Request FAILED: Wrong HTTP Response FORMAT - not a JSON Object ~~> check target URL output ???" + "\n******** Target URL =>  " + url + "  <= ********" + "\n**** Parameters:\n" + debugParameters(" * ", parameters));
        }

        if (responseContainer == null) {
            String statusLine = getStatusLine(responseStatus);
            throw new ServiceIOException("Service Request FAILED with HTTP Error " + statusLine + "\n******** Target URL =>  " + url + "  <= ********" + "\n**** Parameters:\n" + debugParameters(" * ", parameters));
        }

        return responseContainer;
    }

    private static String getStatusLine(Integer responseStatus) {
        String statusLine = "???";
        if (responseStatus != null) {
            statusLine = responseStatus.toString();
            if (responseStatus == 400) {
                statusLine += " - BAD REQUEST ~~> check request parameters ???";
            }
            if (responseStatus == 404) {
                statusLine += " - NOT FOUND ~~> check target URL ???";
            }
            if (responseStatus == 500) {
                statusLine += " - INTERNAL SERVER ERROR ~~> check target Server Log ???";
            }
        }
        return statusLine;
    }

    public static boolean checkJsonObject(JsonElement callResult) {

        return (callResult != null && callResult.isJsonObject());
    }

    public static String debugParameters(String alinea, NameValuePair... parameters) {

        StringBuilder debug = new StringBuilder();

        for (NameValuePair parameter : parameters) {
            debug.append(alinea).append(parameter.getName()).append(" = ").append(parameter.getValue()).append("\n");
        }

        return debug.toString();
    }

}
