package com.practice.logincrud.member;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private int id;
    private String email;
    private String password;
    private LocalDateTime regDate;
    private String address;
    private String detailAddress;
    private String postcode;
    private LocalDateTime updateDate;
}
