package com.lukamaret.pld_mars_ihm.vue;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddAccountVue extends Vue {
    @Override
    public void serialize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int code = (int) request.getAttribute("code");
        response.setStatus(code);

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        if (code == 200) {
            jsonResponse.add("result", gson.toJsonTree("Account created"));
        } else {
            Object error = request.getAttribute("error");
            jsonResponse.addProperty("error", error != null ? error.toString() : "Unknown error");
        }

        response.getWriter().println(gson.toJson(jsonResponse));
    }
}