package com.practice.logincrud;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private int id;
    private String email;
    private String password;
    private LocalDateTime regDate;
    private String address;
    private String detailAddress;
    private String postcode;
    private LocalDateTime updateDate;
}
