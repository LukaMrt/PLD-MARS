package com.lukamaret.pld_mars_address.vue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ListAddressesByAccountVue extends Vue {
    @Override
    public void serialize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int code = (int) request.getAttribute("code");
        response.setStatus(code);

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        if (code == 200) {
            Object addresses = request.getAttribute("addresses");
            jsonResponse.add("addresses", gson.toJsonTree(addresses));
        } else {
            Object error = request.getAttribute("error");
            jsonResponse.addProperty("error", error != null ? error.toString() : "Unknown error");
        }

        response.getWriter().println(gson.toJson(jsonResponse));
    }
}
