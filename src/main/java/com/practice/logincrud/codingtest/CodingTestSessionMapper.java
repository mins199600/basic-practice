package com.practice.logincrud.codingtest;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodingTestSessionMapper {

    void insert(CodingTestSessionDto dto);

    CodingTestSessionDto findById(@Param("id") Long id);

    List<CodingTestSessionDto> findByMemberId(@Param("memberId") Long memberId);

    void updateStatus(@Param("id") Long id, @Param("status") String status);

    // 홈 대시보드 "코딩테스트 준비율" 계산용 - 답안 제출까지 마친(ANSWERED) 세션 개수
    int countAnsweredByMemberId(@Param("memberId") Long memberId);

    // 홈 "오늘의 학습 요약"용 - 오늘/이번 주(월요일 시작)에 시작한 코딩테스트 세션 개수
    int countTodayByMemberId(@Param("memberId") Long memberId);

    int countThisWeekByMemberId(@Param("memberId") Long memberId);
}
