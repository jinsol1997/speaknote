package com.speaknote.speaknote.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(@CookieValue(name = "recorderId", required = false) String recorder_id, HttpServletResponse response) {


        if (recorder_id == null || recorder_id.isBlank()) {
            String id = UUID.randomUUID().toString();

            Cookie cookie = new Cookie("recorderId", id);
            cookie.setPath("/");                       // 모든 경로에서 사용
            cookie.setMaxAge(60 * 60 * 24 * 7);     // 7일짜리 쿠키

            // JS에서 안 쓸 거면 true가 더 안전함 (서버에서만 읽음)
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
        }


        return "forward:/index.html";
    }
}
