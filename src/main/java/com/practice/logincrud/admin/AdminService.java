package com.practice.logincrud.admin;

import com.practice.logincrud.member.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    //관리자 회원가입
    public boolean joinAdmin(String email, String password, String nickName) {

        AdminDto adminDto = new AdminDto();
        adminDto.setEmail(email);
        adminDto.setPassword(passwordEncoder.encode(password));
        adminDto.setNickname(nickName);
        adminDto.setRole("ADMIN");

        int count = adminMapper.insertAdmin(adminDto);

        return count == 1;
    }

    //로그인
    public AdminDto adminAccess(String email, String password) {
        AdminDto admin = adminMapper.adminLogin(email);
        if (admin == null) {
            log.info("관리자 계정 조회 실패");
            return null;
        }

        log.info("관리자 계정 조회 성공 email={}, role={}", admin.getEmail(), admin.getRole());

        if (passwordEncoder.matches(password, admin.getPassword())) {
            log.info("관리자 비밀번호 일치");
            return admin;  // 로그인 성공 → admin 반환
        }

        log.info("관리자 비밀번호 불일치");
        return null;  // 비밀번호 불일치
    }
}
