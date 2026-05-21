package com.practice.logincrud.member;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String nickName;
    private String email;
    private String password;
    private Integer deleted;
}
