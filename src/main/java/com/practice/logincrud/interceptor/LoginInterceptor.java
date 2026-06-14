package com.practice.logincrud.interceptor;

import com.practice.logincrud.config.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        //세션이 있는지 확인(false = 없으면 새로 만들지 말고 null 반환)
        HttpSession session = request.getSession(false);

        //세션이 없거나, 세션에 로그인 정보가 없으면
        if (session == null || session.getAttribute(SessionConst.Member_Id) == null) {
            response.sendRedirect("/");     //로그인 페이지로 강제 이동
            return false;                      //요청 차단(컨트롤러 실행 안됨)
        }

        return true;    //로그인 되어 있으면 통과 -> 컨트롤러 실행
    }
}
