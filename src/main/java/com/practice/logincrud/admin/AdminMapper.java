package com.practice.logincrud.admin;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    //회원가입
    void insertAdmin(AdminDto adminDto);
}
