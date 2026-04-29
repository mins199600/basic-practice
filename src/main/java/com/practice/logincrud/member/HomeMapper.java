package com.practice.logincrud.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HomeMapper {

    //로그인
    User findUserLogin(String email);
    // 회원가입 처리
    void insertMember(User user);
    // 회원가입 이메일 중복 처리
    int countByEmail(String email);
    //회원정보 수정
    void UpdateUser(@Param("UpdateUser") String UpdateUser,
                    @Param("email") String email,
                    @Param("password") String password);
    // 회원 삭제
    void deleteUser(String email);

}
