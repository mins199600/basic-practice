package com.practice.logincrud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class HomeService {
    
    @Autowired
    HomeMapper homeMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //로그인
    public boolean userLogin(String email, String password) {
        User user = homeMapper.findUserLogin(email);

        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
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

    //회원정보 수정
    public void editUser(String updateUser, String email, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        homeMapper.UpdateUser(updateUser, email, encodedPassword);
    }
}

