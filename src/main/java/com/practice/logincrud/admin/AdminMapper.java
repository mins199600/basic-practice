package com.practice.logincrud.admin;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    //회원가입
    int insertAdmin(AdminDto adminDto);

    //로그인
    AdminDto adminLogin(String email);
}
