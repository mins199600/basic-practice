package com.practice.logincrud.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;

    //관리자 회원가입
    public void joinAdmin(AdminDto adminDto) {
        adminMapper.insertAdmin(adminDto);
    }
}
