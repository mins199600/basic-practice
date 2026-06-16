package com.practice.logincrud.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    //회원가입
    int insertAdmin(AdminDto adminDto);

    //로그인
    AdminDto adminLogin(String email);

    //아이디 찾기 - 닉네임으로 이메일 조회
    String findEmailByNickname(String nickname);

    //비밀번호 찾기 - 이메일 + 닉네임으로 계정 확인
    AdminDto findByEmailAndNickname(@Param("email") String email, @Param("nickname") String nickname);

    //비밀번호 변경
    void updatePassword(@Param("email") String email, @Param("password") String password);

}
