package com.practice.logincrud.admin.dashboard;

import lombok.Data;

@Data
public class PopupDto {
    private Long id;
    private String title;
    private String content;
    private String useYn;       // 상단 고정 여부 Y/N
    private String startDate;
    private String endDate;
    private String regDate;
    private String updateDate;
}
