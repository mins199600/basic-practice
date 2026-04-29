package com.practice.logincrud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HomeService {
    
    @Autowired
    HomeMapper homeMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //로그인
    public boolean userLogin(String userId, String userPw) {
        User user = homeMapper.findUserLogin(userId);

        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(userPw, user.getPassword());
    }

    //회원가입
    public boolean join(String email, String password) {
        int count = homeMapper.countByEmail(email);
        if (count > 0) {

            return false;
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        homeMapper.insertMember(user);
        return true;

    }
}

