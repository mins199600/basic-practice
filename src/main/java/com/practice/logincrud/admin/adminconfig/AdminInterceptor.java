package com.practice.logincrud.admin.adminconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 세션 없거나 role이 ADMIN이 아니면 관리자 로그인 페이지로 차단
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("/admin");
            return false;
        }

        return true; // ADMIN이면 통과
    }
}
