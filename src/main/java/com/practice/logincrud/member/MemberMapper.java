package com.practice.logincrud.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {

    //로그인
    UserDto findUserLogin(@Param("email") String email);

    // 회원가입 처리
    void insertMember(UserDto user);

    // 회원가입 이메일 중복 처리
    int countByEmail(@Param("email") String email);

    //회원정보 수정
    UserDto findUserByEmail(@Param("email") String email);

    //회원정보 수정 처리 로직
    void updateUser(@Param("oldEmail") String oldEmail,
                    @Param("user") UserDto userDto);

    //회원정보 삭제
    void deleteUser(@Param("email") String email);

    //전체 회원 수
    int getTotalMemberCount();

    // 전체 회원 목록 조회
    List<UserDto> getAllMembers();

    // 최근 가입 회원 5명
    List<UserDto> getRecentMembers();

}
