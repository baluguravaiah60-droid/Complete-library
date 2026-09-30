package com.library.controller;

import com.library.util.Json;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Every /api/* call except login and me requires a signed-in session. */
@WebFilter("/api/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        String path = req.getServletPath();
        HttpSession s = req.getSession(false);
        boolean open = path.equals("/api/login") || path.equals("/api/me");
        if (open || (s != null && s.getAttribute("user") != null)) chain.doFilter(rq, rs);
        else Json.error(resp, 401, "Please sign in.");
    }
}
