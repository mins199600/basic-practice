package com.practice.logincrud.comment;

import com.practice.logincrud.board.BoardDto;
import com.practice.logincrud.board.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //게시글 작성자 조회용
    private final BoardService boardService;

    // 댓글 작성
    @PostMapping("/comment/write")
    public String insertComment(@RequestParam Long boardId,
                                @RequestParam String content,
                                HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/";

        CommentDto commentDto = new CommentDto();
        commentDto.setBoardId(boardId);
        commentDto.setMemberId(memberId);
        commentDto.setContent(content);

        commentService.insertComment(commentDto);

        return "redirect:/board/view/" + boardId;
    }

    // 대댓글 작성
    @PostMapping("/comment/reply")
    public String insertReply(@RequestParam Long boardId,
                              @RequestParam Long parentId,
                              @RequestParam String content,
                              HttpSession session) {

        Long memberId = (Long)session.getAttribute("memberId");
        if (memberId == null) return "redirect:/";

        CommentDto commentDto = new CommentDto();
        commentDto.setBoardId(boardId);
        commentDto.setMemberId(memberId);
        commentDto.setParentId(parentId);
        commentDto.setContent(content);

        commentService.insertReply(commentDto);

        return "redirect:/board/view/" + boardId;
    }

    // 댓글 수정
    @PostMapping("/comment/update")
    public String updateComment(@RequestParam Long id,
                                @RequestParam Long boardId,
                                @RequestParam String content,
                                HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/";

        commentService.updateComment(id, content, memberId);

        return "redirect:/board/view/" + boardId;
    }

    // 댓글 삭제
    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long id,
                                @RequestParam Long boardId,
                                HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/";

        String role = (String) session.getAttribute("role"); // 세션에서 role 꺼내기

        // 게시글 작성자 id 조회
        BoardDto board = boardService.findById(boardId);
        Long boardWriterId = board.getMemberId();

        commentService.deleteComment(id, memberId, role, boardWriterId);

        return "redirect:/board/view/" + boardId;
    }
}
