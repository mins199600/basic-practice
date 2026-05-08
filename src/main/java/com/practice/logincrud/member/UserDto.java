package com.practice.logincrud.member;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private int id;
    private String nickname;
    private String email;
    private String password;
}
