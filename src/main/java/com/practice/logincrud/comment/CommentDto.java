package com.practice.logincrud.comment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDto {
    private Long id;
    private Long boardId;
    private Long memberId;
    private Long parentId;
    private String content;
    private String nickName;
    private String createdAt;

    // 대댓글 목록을 담을 리스트
    private List<CommentDto> children = new ArrayList<>();
}
