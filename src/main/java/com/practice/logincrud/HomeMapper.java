package com.practice.logincrud;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeMapper {

    User findUserLogin(String email);

    void insertMember(User user);   // 회원가입 처리
    int countByEmail(String email); // 회원가입 이메일 중복 처리
}
