package com.practice.logincrud.board;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardDto {
    private int id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int viewCount;
    private String category;
    private boolean isNotice;
    private String filePath;
    private int memberId;
}
