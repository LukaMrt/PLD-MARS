package com.lukamaret.pld_mars_account.vue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class Vue {
    public Vue() {
    }

    public abstract void serialize(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
