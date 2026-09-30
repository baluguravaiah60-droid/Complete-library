package com.library.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public final class Json {
    private static final Gson GSON = new Gson();
    private Json() {}
    public static void send(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }
    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        send(resp, status, Map.of("error", message));
    }
}
