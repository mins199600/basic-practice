package com.practice.logincrud.comment;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 댓글 + 대댓글 전체 조회
    List<CommentDto> getCommentList(Long boardId);

    // 댓글 작성 (parent_id = null)
    void insertComment(CommentDto commentDto);

    // 대댓글 작성 (parent_id = 부모 댓글 id)
    void insertReply(CommentDto commentDto);

    // 수정 기능 추가
    void updateComment(CommentDto commentDto);

    // 댓글 삭제
    void deleteComment(Long id);

    // 댓글 작성자 조회 (삭제 권한 체크용)
    Long getCommentMemberId(Long id);
}
