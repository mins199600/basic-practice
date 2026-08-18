package com.practice.logincrud.board;

import com.practice.logincrud.admin.AdminRole;
import com.practice.logincrud.attendance.AttendanceService;
import com.practice.logincrud.certification.CertificationService;
import com.practice.logincrud.codingtest.test.CodingTestService;
import com.practice.logincrud.comment.CommentDto;
import com.practice.logincrud.comment.CommentService;
import com.practice.logincrud.interview.InterviewService;
import com.practice.logincrud.member.MemberService;
import com.practice.logincrud.member.UserDto;
import com.practice.logincrud.stats.SkillStatsDto;
import com.practice.logincrud.stats.StatsService;
import com.practice.logincrud.stats.StudySummaryDto;
import com.practice.logincrud.stats.StudySummaryService;
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
    private final CertificationService certificationService;
    private final AttendanceService attendanceService;
    private final InterviewService interviewService;
    private final CodingTestService codingTestService;
    private final MemberService memberService;
    private final StatsService statsService;
    private final StudySummaryService studySummaryService;

    // 홈 = 게시글 목록 + 페이지네이션
    @GetMapping("/home")
    public String home(PageDto pageDto,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String sort,
                       Model model, HttpSession session) {


        String nickName = (String) session.getAttribute("nickName");
        String email = (String) session.getAttribute("email");
        Long memberId = (Long) session.getAttribute("memberId");
        List<BoardDto> boardList;
        int totalCount;

        // 자격증 / 출석 대시보드 데이터
        int certTotalCount = certificationService.getTotalCount(memberId);
        int certPassRate = certificationService.getPassRate(memberId);
        boolean attendedToday = attendanceService.hasAttendedToday(memberId);
        int attendanceStreak = attendanceService.getCurrentStreak(memberId);
        int attendanceRate = attendanceService.getAttendanceRate(memberId);

        model.addAttribute("certTotalCount", certTotalCount);
        model.addAttribute("certPassRate", certPassRate);
        model.addAttribute("attendedToday", attendedToday);
        model.addAttribute("attendanceStreak", attendanceStreak);
        model.addAttribute("attendanceRate", attendanceRate);

        // 실력 분석 & 취업 성공률 - 각 항목을 실제 데이터로 계산해서 종합
        int interviewPrepRate = interviewService.getPrepRate(memberId);
        int codingTestRate = codingTestService.getPrepRate(memberId);
        SkillStatsDto skillStats = statsService.calculate(certPassRate, interviewPrepRate, codingTestRate, attendanceRate);
        model.addAttribute("skillStats", skillStats);
        model.addAttribute("codingTestRate", codingTestRate);

        // 오늘의 학습 요약 (대시보드 카드 - 프로필 사진 대신 노출)
        StudySummaryDto studySummary = studySummaryService.getSummary(memberId);
        model.addAttribute("studySummary", studySummary);

        // 상단 카드의 작은 프로필 사진 (클릭해서 바로 변경 가능)
        UserDto currentUser = memberService.findUserByEmail(email);
        model.addAttribute("profileImageUrl", currentUser != null ? currentUser.getProfileImage() : null);

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

            //검색어 없을 때 -> 필터 적용 조회
            pageDto.setCategory(category);
            pageDto.setSort(sort);

            // 검색어 없을 때 → 전체 조회
            boardList = boardService.getMyBoardList(pageDto);
            totalCount = boardService.getMyBoardTotalCountByFilter(pageDto);
        }

        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageSize());

        model.addAttribute("boardList", boardList);
        model.addAttribute("startNo", pageDto.getStartNo());
        model.addAttribute("currentPage", pageDto.getPage());
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("nickName", nickName);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);

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

        Long memberId = (Long) session.getAttribute("memberId");

        boardService.increaseViewCount(id); // 조회수 증가

        BoardDto board = boardService.findById(id);
        if (board == null) {
            return "redirect:/home";
        }

        List<CommentDto> commentList = commentService.getCommentList(id);
        AdminRole role = AdminRole.fromRaw(session.getAttribute("role"));

        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);
        model.addAttribute("loginMemberId", memberId);
        model.addAttribute("isAdmin", role != null && role.isAdminOrAbove());

        boolean isAuthor = board.getMemberId() != null && board.getMemberId().equals(memberId);
        model.addAttribute("isAuthor", isAuthor);

        return "board/detail";
    }

    // 글쓰기 화면 이동
    @GetMapping("/board/create")
    public String createForm() {

        return "board/create";
    }

    // 글쓰기 저장
    @PostMapping("/board/create")
    public String create(BoardDto boardDto, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");

        boardDto.setMemberId(memberId);
        boardService.save(boardDto);

        return "redirect:/home";
    }

    // 게시글 수정 화면
    @GetMapping("/board/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {

        Long memberId = (Long) session.getAttribute("memberId");

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
