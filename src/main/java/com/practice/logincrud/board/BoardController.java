package com.practice.logincrud.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 홈 = 게시글 목록 + 페이지네이션
    @GetMapping("/home")
    public String home(PageDto pageDto, Model model, HttpSession session) {

        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        int totalCount = boardService.getBoardTotalCount();
        List<BoardDto> boardList = boardService.getBoardList(pageDto);

        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageSize());

        model.addAttribute("boardList", boardList);
        model.addAttribute("startNo", pageDto.getStartNo());
        model.addAttribute("currentPage", pageDto.getPage());
        model.addAttribute("totalPage", totalPage);

        return "home";
    }

    // 게시글 목록 페이지는 home으로 이동
    @GetMapping("/board/list")
    public String boardList() {
        return "redirect:/home";
    }

    // 상세조회
    @GetMapping("/board/view/{id}")
    public String detail(@PathVariable Integer id, Model model, HttpSession session) {
        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        BoardDto board = boardService.findById(id);

        if (board == null) {
            return "redirect:/home";
        }

        model.addAttribute("board", board);

        boolean isAuthor = board.getMemberId().equals(memberId);
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

        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        boardDto.setMemberId(memberId);
        boardService.save(boardDto);

        return "redirect:/home";
    }

    // 게시글 수정 화면
    @GetMapping("/board/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, HttpSession session) {

        Integer memberId = (Integer) session.getAttribute("memberId");
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
    public String edit(@PathVariable Integer id, BoardDto boardDto, HttpSession session) {

        Integer memberId = (Integer) session.getAttribute("memberId");
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
    public String delete(@PathVariable Integer id, HttpSession session) {
        Integer memberId = (Integer) session.getAttribute("memberId");

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

}
