package com.speaknote.speaknote.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RecorderIdFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "recorderId";
    private static final int MAX_AGE = 60 * 60 * 24 * 7;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String recorderId = getCookieValue(request);

        if (!isValidUuid(recorderId)) {
            recorderId = UUID.randomUUID().toString();
        }

        // 쿠키 기존값 또는 신규로 7일 갱신
        Cookie cookie = new Cookie(COOKIE_NAME, recorderId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        response.addCookie(cookie);

        // 스레드에 로그에 쓸 식별자 부여
        MDC.put(COOKIE_NAME, recorderId);

        log.info("cookie 갱신, uri : {}",  request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 식별자 해제
            MDC.clear();
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 정적 리소스 & chrome 개발자툴 필터에서 무시
        return uri.startsWith("/js/") || uri.startsWith("/css/") || uri.startsWith("/images/")
                || uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".ico")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".svg") || uri.endsWith(".gif") || uri.startsWith("/.well-known/");
    }

    private String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
