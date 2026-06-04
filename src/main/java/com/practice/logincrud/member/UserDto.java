package com.practice.logincrud.member;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;   //로그인 아이디
    private String role;    //관리자 유저 역할
    private String nickName;
    private String password;
    private Integer deleted;
}
