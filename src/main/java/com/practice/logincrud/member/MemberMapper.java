package com.practice.logincrud.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    //로그인
    UserDto findUserLogin(String email);

    // 회원가입 처리
    void insertMember(UserDto user);

    // 회원가입 이메일 중복 처리
    int countByEmail(String email);

    //회원정보 수정
    UserDto findUserByEmail(@Param("email") String email);

    //회원정보 수정 처리 로직
    void updateUser(@Param("oldEmail") String oldEmail,
                    @Param("user") UserDto userDto);

    //회원정보 삭제
    void deleteUser(@Param("email") String email);
}
