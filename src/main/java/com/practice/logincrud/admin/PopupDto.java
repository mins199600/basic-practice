package com.practice.logincrud.admin;

import lombok.Data;

@Data
public class PopupDto {
    private Long id;
    private String title;
    private String content;
    private String fixedYn;     // 상단 고정 여부 Y/N
    private String regDate;
    private String updateDate;
}
