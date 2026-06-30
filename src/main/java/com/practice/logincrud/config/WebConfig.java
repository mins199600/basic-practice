package com.practice.logincrud.config;

import com.practice.logincrud.admin.adminconfig.AdminInterceptor;
import com.practice.logincrud.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")     //모든 요청을 검사
                .excludePathPatterns(       //여기 있는건 검사 안함
                        "/",                //로그인 안한 사람 접근 가능해야 하기 때문에
                        "/login",           //로그인 처리 요청, -> 요청을 막게 되면 로그인이 안됨
                        "/join",            // 회원가입 페이지 이동 요청 -> 로그인전 사용자가 회원가입 하기 위해
                        "/signup",          //회원가입 form을 제출하는 요청이므로 로그인전에 가능해야 함
                        "/admin",           //관리자도 로그인전에 봐야 하므로 예외처리(관리자 메인페이지)
                        "/admin/join",      // 관리자도 계정을 만들어서 사용해야 하기 때문에
                        "/admin/login",     // 관리자 로그인 요청 -> 로그인 버튼 눌렀을때 인터셉트 처리 예외
                        "/admin/**",
                        "/css/**",          // 로그인 여부와 상관 없기 때문에 브라우저가 불러와야 화면에 정상적으로 표시됨
                        "/js/**",
                        "/images/**",
                        "/api/user-info",    //사용자 정보를 JS에서 호출하는데, 로그인 안 된 상태에서도 로그인 안함을 반환 해야 하기 때문에
                        "/email/send-code",
                        "/email/verify-code"
                );

        // 2. 관리자 전용 인터셉터 (ADMIN role 체크)
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**")   // /admin 하위 전부 검사
                .excludePathPatterns(
                        "/admin",           // 관리자 로그인 페이지
                        "/admin/login",     // 관리자 로그인 처리
                        "/admin/join",      // 관리자 회원가입
                        "/admin/find-id",    // 관리자 아이디 찾기
                        "/admin/find-password",  //관리자 비번 찾기
                        "/admin/reset-password"

                );
    }
}



