package com.practice.logincrud.attendance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceService {

    private static final int RATE_WINDOW_DAYS = 30;
    private static final int CALENDAR_WINDOW_DAYS = 35; // 5주치 그리드

    private final AttendanceMapper attendanceMapper;

    // 오늘 출석했는지 여부
    public boolean hasAttendedToday(Long memberId) {
        return attendanceMapper.existsOnDate(memberId, LocalDate.now()) > 0;
    }

    // 출석 체크인 (하루 1회만 기록)
    public void checkIn(Long memberId) {
        if (hasAttendedToday(memberId)) {
            log.info("이미 오늘 출석함 memberId={}", memberId);
            return;
        }
        attendanceMapper.insert(memberId, LocalDate.now());
        log.info("출석 체크인 완료 memberId={}", memberId);
    }

    // 최근 30일 출석률(%)
    public int getAttendanceRate(Long memberId) {
        LocalDate since = LocalDate.now().minusDays(RATE_WINDOW_DAYS - 1);
        int count = attendanceMapper.countSince(memberId, since);
        return (int) Math.round(count * 100.0 / RATE_WINDOW_DAYS);
    }

    // 현재 연속 출석일 수 (오늘 미출석이면 어제 기준으로 계산)
    public int getCurrentStreak(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(90); // 넉넉하게 90일치로 계산
        Set<LocalDate> attendedDates = new HashSet<>(attendanceMapper.findDatesBetween(memberId, start, today));

        LocalDate cursor = attendedDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (attendedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    // 최근 35일 출석 여부 그리드 (오래된 날짜 -> 오늘 순)
    public List<LocalDate> getCalendarAttendedDates(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(CALENDAR_WINDOW_DAYS - 1);
        return attendanceMapper.findDatesBetween(memberId, start, today);
    }

    public int getCalendarWindowDays() {
        return CALENDAR_WINDOW_DAYS;
    }
}
