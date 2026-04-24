package com.practice.logincrud;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeMapper {

    User findUserLogin(String userId);
}
