package com.practice.logincrud.attendance;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // 출석 캘린더 페이지
    @GetMapping("/attendance")
    public String calendar(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");

        List<LocalDate> attendedDates = attendanceService.getCalendarAttendedDates(memberId);
        Set<LocalDate> attendedSet = attendedDates.stream().collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(attendanceService.getCalendarWindowDays() - 1);

        model.addAttribute("start", start);
        model.addAttribute("today", today);
        model.addAttribute("attendedSet", attendedSet);
        model.addAttribute("attendedToday", attendanceService.hasAttendedToday(memberId));
        model.addAttribute("streak", attendanceService.getCurrentStreak(memberId));
        model.addAttribute("rate", attendanceService.getAttendanceRate(memberId));

        return "attendance/calendar";
    }

    // 출석 체크인 처리 (홈 대시보드 / 캘린더 페이지 양쪽에서 호출)
    @PostMapping("/attendance/check-in")
    public String checkIn(@RequestParam(defaultValue = "/home") String redirectTo, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        attendanceService.checkIn(memberId);
        return "redirect:" + redirectTo;
    }
}
