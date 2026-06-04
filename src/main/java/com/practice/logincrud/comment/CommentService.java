package com.practice.logincrud.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    // 댓글 목록 조회 (대댓글 분리 포함)
    public List<CommentDto> getCommentList(Long boardId) {
        List<CommentDto> allComments = commentMapper.getCommentList(boardId);

        // parent_id = null 인 댓글만 추출
        List<CommentDto> parents = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        // 대댓글을 부모 댓글의 children 리스트에 추가
        for (CommentDto parent : parents) {
            List<CommentDto> children = allComments.stream()
                    .filter(c -> c.getParentId() != null
                            && c.getParentId().equals(parent.getId()))
                    .collect(Collectors.toList());
            parent.setChildren(children);
        }

        return parents;
    }

    // 댓글 작성
    public void insertComment(CommentDto commentDto) {
        commentMapper.insertComment(commentDto);
    }

    // 대댓글 작성
    public void insertReply(CommentDto commentDto) {
        commentMapper.insertReply(commentDto);
    }

    // 수정 권한 체크 후 수정
    public boolean updateComment(Long id, String content, Long memberId) {
        Long commentWriterId = commentMapper.getCommentMemberId(id);

        if (commentWriterId.equals(memberId)) { // 본인만 수정 가능
            CommentDto dto = new CommentDto();
            dto.setId(id);
            dto.setContent(content);
            commentMapper.updateComment(dto);
            return true;
        }
        return false; // 권한 없음
    }

    // 댓글 삭제
    public void deleteComment(Long id) {
        commentMapper.deleteComment(id);
    }

    // 삭제 권한 체크 후 삭제
    public boolean deleteComment(Long id, Long memberId, String role, Long boardWriterId) {
        Long commentWriterId = commentMapper.getCommentMemberId(id);

        boolean isAdmin       = "ADMIN".equals(role);
        boolean isBoardWriter = boardWriterId.equals(memberId);  // 게시글 작성자
        boolean isCommentWriter = commentWriterId.equals(memberId); // 댓글 작성자 (본인)

        if (isAdmin || isBoardWriter || isCommentWriter) {
            commentMapper.deleteComment(id);
            return true;
        }
        return false; // 권한 없음
    }


}
