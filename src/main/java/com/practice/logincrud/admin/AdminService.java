package com.practice.logincrud.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    //관리자 회원가입
    public boolean joinAdmin(String email, String password, String nickName) {
        int count = adminMapper.insertAdmin();

        if(count == 0) {
            return false;
        }

        AdminDto adminDto = new AdminDto();
        adminDto.setEmail(email);
        adminDto.setPassword(passwordEncoder.encode(password));
        adminDto.setNickname(nickName);
        adminMapper.insertAdmin(adminDto);
        return true;
    }

}
