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


/**
 * 요청마다 recorderId 쿠키를 관리하고,
 * 로그 추적을 위해 MDC에 식별자를 설정하는 요청당 한 번 실행되는 필터.
 *
 * <p>주요 역할:</p>
 * <ul>
 *   <li>recorderId 쿠키 존재 여부 확인</li>
 *   <li>유효하지 않거나 없는 경우 UUID 신규 발급</li>
 *   <li>쿠키를 7일 만료로 갱신</li>
 *   <li>MDC에 recorderId를 저장하여 로그 추적 가능하게 처리</li>
 * </ul>
 *
 * <p>적용 범위:</p>
 * <ul>
 *   <li>API 및 일반 요청에만 적용</li>
 *   <li>정적 리소스 요청은 필터 대상에서 제외</li>
 * </ul>
 */
@Slf4j
@Component
public class RecorderIdFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "recorderId";
    private static final int MAX_AGE = 60 * 60 * 24 * 7;


    /**
     * <p>처리 흐름:</p>
     * <ul>
     *   <li>요청 쿠키에서 recorderId 조회</li>
     *   <li>UUID 형식 검증</li>
     *   <li>유효하지 않으면 신규 UUID 발급</li>
     *   <li>쿠키 갱신 및 응답에 설정</li>
     *   <li>MDC에 recorderId 저장</li>
     * </ul>
     */
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


    /**
     * 필터를 적용하지 않을 요청 경로를 지정
     *
     * <p>대상 제외:</p>
     * <ul>
     *   <li>정적 리소스 (js, css, images 등)</li>
     *   <li>chrome 개발자 툴에서 보내는 요청</li>
     * </ul>
     *
     * @return 필터 제외 조건
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 정적 리소스 & chrome 개발자툴 필터에서 무시
        return uri.startsWith("/js/") || uri.startsWith("/css/") || uri.startsWith("/images/")
                || uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".ico")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".svg") || uri.endsWith(".gif") || uri.startsWith("/.well-known/");
    }


    /**
     * 요청 쿠키에서 recorderId 값을 조회한다.
     *
     * @return recorderId 쿠키 값, 존재하지 않으면 null
     */
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


    /**
     * 문자열이 UUID 형식인지 검증한다.
     *
     * @param value 검증할 문자열
     * @return UUID 형식이면 true, 아니면 false
     */
    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
