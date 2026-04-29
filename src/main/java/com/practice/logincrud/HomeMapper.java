package com.practice.logincrud;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.ui.Model;

@Mapper
public interface HomeMapper {

    User findUserLogin(String email);   //로그인
    void insertMember(User user);       // 회원가입 처리
    int countByEmail(String email);     // 회원가입 이메일 중복 처리
    void UpdateUser(@Param("UpdateUser") String UpdateUser,
                    @Param("email") String email,
                    @Param("password") String password); //회원정보 수정
}
