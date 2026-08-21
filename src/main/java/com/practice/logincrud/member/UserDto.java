package com.practice.logincrud.member;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;   //로그인 아이디
    private String role;    //관리자 유저 역할
    private String nickName;
    private String password;
    private String profileImage;  // 프로필 사진 URL (예: /uploads/profile/xxx.jpg), 없으면 null
    private Integer deleted;
    private LocalDateTime regDate;  // 가입일시
}
