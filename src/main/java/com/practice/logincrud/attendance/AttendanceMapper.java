package com.practice.logincrud.attendance;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceMapper {

    // 출석 체크인
    void insert(@Param("memberId") Long memberId, @Param("attendDate") LocalDate attendDate);

    // 오늘 이미 출석했는지 여부
    int existsOnDate(@Param("memberId") Long memberId, @Param("attendDate") LocalDate attendDate);

    // 기간 내 출석 날짜 목록 (달력 표시 / 연속 출석 계산용)
    List<LocalDate> findDatesBetween(@Param("memberId") Long memberId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    // 최근 N일 중 출석 횟수
    int countSince(@Param("memberId") Long memberId, @Param("sinceDate") LocalDate sinceDate);
}
