package com.practice.logincrud.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.ast.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //전체조회

    //상세조회

    // 글쓰기 화면 이동
    @GetMapping("/board/create")
    public String createForm(HttpSession session) {

        Object memberId = session.getAttribute("memberId");
        Object nickname = session.getAttribute("nickname");
        Object email = session.getAttribute("email");

        System.out.println("memberId = " + memberId);
        System.out.println("nickname = " + nickname);
        System.out.println("email = " + email);

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
