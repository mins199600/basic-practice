package com.practice.logincrud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeService {
    
    @Autowired
    HomeMapper homeMapper;

    public boolean userLogin(String userId, String userPw) {
        User user = homeMapper.findUserLogin(userId);

        if (user == null) {
            return false;
        }
        return user.getPassword().equals(userPw);
    }


}
