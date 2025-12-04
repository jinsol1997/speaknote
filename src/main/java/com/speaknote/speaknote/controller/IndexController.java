package com.speaknote.speaknote.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) {

        boolean hasRecorderId = false;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("recorder_id".equals(c.getName())) {
                    hasRecorderId = true;
                    break;
                }
            }
        }

        if (!hasRecorderId) {
            String id = UUID.randomUUID().toString();

            Cookie cookie = new Cookie("recorder_id", id);
            cookie.setPath("/");                       // 모든 경로에서 사용
            cookie.setMaxAge(60 * 60 * 24 * 365);     // 1년짜리 쿠키

            // JS에서 안 쓸 거면 true가 더 안전함 (서버에서만 읽음)
            cookie.setHttpOnly(true);

            // 로컬 개발 단계면 주석, HTTPS 환경이면 true 권장
            // cookie.setSecure(true);

            response.addCookie(cookie);
        }


        return "forward:/index.html";
    }
}
