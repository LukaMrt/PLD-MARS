package com.lukamaret.pld_mars_ihm.vue;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddAccountVue extends Vue {
    @Override
    public void serialize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int code = (int) request.getAttribute("code");
        response.setStatus(code);
        Object result;

        if (code == 201) {
            result = "Account created";
        } else {
            result = request.getAttribute("error");
        }

        response.getWriter().println(new Gson().toJson(result));
    }
}