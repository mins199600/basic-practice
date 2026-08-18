package com.practice.logincrud.admin.adminconfig;

import com.practice.logincrud.admin.AdminRole;
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
        Object rawRole = session == null ? null : session.getAttribute("role");
        AdminRole role = AdminRole.fromRaw(rawRole);

        // 세션 없거나 role이 ADMIN/최고관리자(2)가 아니면 관리자 로그인 페이지로 차단
        if (role == null || !role.isAdminOrAbove()) {
            response.sendRedirect("/admin");
            return false;
        }

        return true;
    }
}
