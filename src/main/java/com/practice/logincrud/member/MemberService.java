package com.practice.logincrud.member;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    
    @Autowired
    MemberMapper memberMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //로그인
    public boolean userLogin(String userId, String userPw) {
        UserDto user = memberMapper.findUserLogin(userId);

        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(userPw, user.getPassword());
    }

    //회원가입
    public boolean join(String email, String password) {
        int count = memberMapper.countByEmail(email);
        if (count > 0) {

            return false;
        }
        UserDto user = new UserDto();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        memberMapper.insertMember(user);
        return true;

    }

    //회원정보 수정
    public void editUser(String updateUser, String email, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        memberMapper.UpdateUser(updateUser, email, encodedPassword);
    }

    // 회원 삭제
    public void deleteUser(String email) {
        memberMapper.deleteUser(email);
    }

}

