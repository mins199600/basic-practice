package com.practice.logincrud.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MemberService {

    @Autowired
    MemberMapper memberMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //로그인
    public UserDto userLogin(String email, String password) {
        UserDto user = memberMapper.findUserLogin(email);

        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;  // 로그인 성공 → user 반환
        }

        return null;  // 비밀번호 불일치
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
        memberMapper.deleteUser(email);
        log.info("회원 soft delete 완료");
    }


}