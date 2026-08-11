package com.practice.logincrud.interview.message;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewMessageMapper {

    void insert(InterviewMessageDto dto);

    // 세션의 전체 대화 이력 (시간순) — 화면 렌더링 + AI 프롬프트 컨텍스트 구성에 공용으로 사용
    List<InterviewMessageDto> findBySessionId(@Param("sessionId") Long sessionId);

    // 회원이 지금까지 제출한 답변 개수 (홈 대시보드 "면접 준비율" 계산용)
    int countAnsweredByMemberId(@Param("memberId") Long memberId);
}
