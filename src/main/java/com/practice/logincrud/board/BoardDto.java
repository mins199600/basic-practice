package com.practice.logincrud.board;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardDto {
    private Integer id;
    private Integer memberId;
    private String title;
    private String content;
    private String nickName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int viewCount;
    private String category;
    private boolean isNotice;
    private String filePath;

}
