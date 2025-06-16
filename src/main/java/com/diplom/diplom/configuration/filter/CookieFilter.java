package com.diplom.diplom.configuration.filter;

import com.diplom.diplom.configuration.ConfPropsCookies;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CookieFilter extends HttpFilter {
    private final ConfPropsCookies appcookies;

    @Autowired
    public CookieFilter(ConfPropsCookies appcookies) {
        this.appcookies = appcookies;
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        Cookie[] cookies = req.getCookies();
        boolean hasSessionCookie = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (appcookies.getCookie_id().equals(cookie.getName())) {
                    hasSessionCookie = true;
                    break;
                }
            }
        }
        if (!hasSessionCookie) {
            Cookie cookie = new Cookie(appcookies.getCookie_id(), generateUniqueSessionId());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(90 * 24 * 60 * 60);
            cookie.setAttribute("SameSite","Strict");
           /* res.setHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue() + "; Path=" + cookie.getPath()
                    + "; HttpOnly; Secure; SameSite=None");*/
            res.addCookie(cookie);
        }
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
    }

    private String generateUniqueSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
}
