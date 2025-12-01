package com.lukamaret.pld_mars_ihm.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;

public abstract class Action {
    public abstract void execute(HttpServletRequest request);

    protected JsonObject getBody(HttpServletRequest request) {
        JsonObject json = new JsonObject();
        try {
            json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
