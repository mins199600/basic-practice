package com.practice.logincrud.board;

import com.practice.logincrud.comment.CommentDto;
import com.practice.logincrud.comment.CommentMapper;
import com.practice.logincrud.comment.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // 홈 = 게시글 목록 + 페이지네이션
    @GetMapping("/home")
    public String home(PageDto pageDto,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       Model model, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        String nickName = (String) session.getAttribute("nickName");
        List<BoardDto> boardList;
        int totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {

            // 검색어 있을 때
            Map<String, Object> params = new HashMap<>();
            params.put("searchType", searchType);
            params.put("keyword", keyword);
            params.put("pageSize", pageDto.getPageSize());
            params.put("offset", pageDto.getOffset());

            boardList = boardService.getBoardList(params);
            totalCount = boardService.getBoardSearchCount(params);
        } else {
            // 검색어 없을 때 → 전체 조회
            boardList = boardService.getMyBoardList(pageDto);
            totalCount = boardService.getMyBoardTotalCount();
        }

        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageSize());

        model.addAttribute("boardList", boardList);
        model.addAttribute("startNo", pageDto.getStartNo());
        model.addAttribute("currentPage", pageDto.getPage());
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("nickName", nickName);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "home";
    }

    // 게시글 목록 페이지는 home으로 이동
    @GetMapping("/board/list")
    public String boardList() {
        return "redirect:/home";
    }

    // 상세조회
    @GetMapping("/board/view/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId"); // ← memberId 로 받음
        if (memberId == null) {
            return "redirect:/";
        }

        BoardDto board = boardService.findById(id);
        if (board == null) {
            return "redirect:/home";
        }

        List<CommentDto> commentList = commentService.getCommentList(id); // intValue() 제거
        String role = (String) session.getAttribute("role");

        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);
        model.addAttribute("loginMemberId", memberId);
        model.addAttribute("isAdmin", "ADMIN".equals(role));

        boolean isAuthor = board.getMemberId() != null && board.getMemberId().equals(memberId); // ← memberId 사용
        model.addAttribute("isAuthor", isAuthor);

        return "board/detail";
    }



    // 글쓰기 화면 이동
    @GetMapping("/board/create")
    public String createForm(HttpSession session) {

        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        return "board/create";
    }

    // 글쓰기 저장
    @PostMapping("/board/create")
    public String create(BoardDto boardDto, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        boardDto.setMemberId(memberId);
        boardService.save(boardDto);

        return "redirect:/home";
    }

    // 게시글 수정 화면
    @GetMapping("/board/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        BoardDto board = boardService.findById(id);

        if (board == null) {
            return "redirect:/home";
        }

        if (!board.getMemberId().equals(memberId)) {
            return "redirect:/board/view/" + id;
        }

        model.addAttribute("board", board);
        return "board/edit";
    }

    // 게시글 수정 처리
    @PostMapping("/board/edit/{id}")
    public String edit(@PathVariable Long id, BoardDto boardDto, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        BoardDto board = boardService.findById(id);

        if (board == null) {
            return "redirect:/home";
        }

        if (!board.getMemberId().equals(memberId)) {
            return "redirect:/board/view/" + id;
        }

        boardDto.setId(id);
        boardService.update(boardDto);

        return "redirect:/board/view/" + id;
    }

    // 게시글 삭제
    @PostMapping("/board/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");

        if (memberId == null) {
            return "redirect:/login";
        }

        BoardDto board = boardService.findById(id);
        if (board == null) {
            return "redirect:/home";
        }

        if (!board.getMemberId().equals(memberId)) {
            return "redirect:/board/view/" + id;
        }

        boardService.delete(id);
        return "redirect:/home";
    }

    // 검색어 처리
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String searchType,
                         @RequestParam(required = false) String keyword) {
        return "redirect:/home?searchType=" + (searchType != null ? searchType : "title")
                + "&keyword=" + (keyword != null ? keyword : "");
    }

}
