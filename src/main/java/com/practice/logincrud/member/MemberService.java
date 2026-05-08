package com.practice.logincrud.member;

import org.apache.catalina.User;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
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


    // 회원정보 조회
    public UserDto findUserByEmail(String email) {
        return memberMapper.findUserByEmail(email);
    }


    // 회원정보 수정
    public boolean updateUser(String oldEmail, UserDto userDto) {

        String newEmail = userDto.getEmail();

        // 이메일을 변경한 경우에만 중복 검사
        if (!oldEmail.equals(newEmail)) {
            int count = memberMapper.countByEmail(newEmail);

            if (count > 0) {
                return false;
            }
        }

        String password = userDto.getPassword();

        if (password != null && !password.trim().isEmpty()) {
            userDto.setPassword(passwordEncoder.encode(password));
        } else {
            userDto.setPassword(null);
        }

        memberMapper.updateUser(oldEmail, userDto);

        return true;
    }

    // 회원 삭제
    public void deleteUser(String email) {
        log.info("회원 삭제 성공");
        memberMapper.deleteUser(email);
    }


}

