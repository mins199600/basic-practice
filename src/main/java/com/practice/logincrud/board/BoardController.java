package com.practice.logincrud.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.ast.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //전체조회
    @GetMapping("/home")
    public String home(Model model, HttpSession session) {

        // 세션 확인
        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        // 게시글 전체 조회
        List<BoardDto> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "home";
    }

    //상세조회
    @GetMapping("/board/view/{id}")
    public String detail(@PathVariable Integer id, Model model, HttpSession session) {

        // 세션 확인
        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/";
        }

        // 게시글 조회
        BoardDto board = boardService.findById(id);

        if (board == null) {
            return "redirect:/home";
        }

        model.addAttribute("board", board);

        // 작성자 본인 확인 (수정/삭제 버튼 노출용)
        boolean isAuthor = board.getMemberId().equals(memberId);
        model.addAttribute("isAuthor", isAuthor);

        return "board/detail";
    }

    // 글쓰기 화면 이동
    @GetMapping("/board/create")
    public String createForm(HttpSession session) {

        Object memberId = session.getAttribute("memberId");
        Object nickname = session.getAttribute("nickname");
        Object email = session.getAttribute("email");

        log.info("memberId = " + memberId);
        log.info("nickname = " + nickname);
        log.info("email = " + email);

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

    //게시글 수정

    //게시글 삭제
}
