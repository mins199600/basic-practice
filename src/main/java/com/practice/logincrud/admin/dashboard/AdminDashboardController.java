package com.practice.logincrud.admin.dashboard;

import com.practice.logincrud.board.BoardService;
import com.practice.logincrud.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);
    private final AdminDashboardService adminDashboardService;  // 공지사항
    private final BoardService boardService;                    // 전체 게시글
    private final MemberService memberService;                  // 전체 회원

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        int totalBoard  = boardService.getMyBoardTotalCount();          // 회원 전체 게시글 수
        int totalMember = memberService.getTotalMemberCount();          // 전체 회원 수
        int totalNotice = adminDashboardService.getNoticeBoardCount();  // 관리자 공지 사항

        model.addAttribute("totalBoard",  totalBoard);
        model.addAttribute("totalMember", totalMember);
        model.addAttribute("totalNotice", totalNotice);

        log.info("totalBoard: " + totalBoard);
        log.info("totalMember: " + totalMember);
        log.info("totalNotice: " + totalNotice);

        return "admin/dashboard";
    }

}
