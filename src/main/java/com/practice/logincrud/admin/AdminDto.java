package com.practice.logincrud.admin;

import lombok.Data;

@Data
public class AdminDto {
    private Long id;
    private String email;
    private String nickname;
    private String password;
    private String role;
    private String regDate;     //가입일자
    private String updateDate;  //수정일자

}
