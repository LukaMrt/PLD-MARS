package com.lukamaret.pld_mars_account.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

public abstract class Action {
    public abstract void execute(HttpServletRequest request) throws ServiceException;

    protected JsonObject getBody(HttpServletRequest request) {
        JsonObject json;
        try {
            JsonElement jsonElement = JsonParser.parseReader(request.getReader());
            if (!jsonElement.isJsonObject()) {
                return null;
            }
            json = jsonElement.getAsJsonObject();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return json;
    }
}
