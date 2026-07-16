package com.practice.logincrud.interview;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewMessageMapper {

    void insert(InterviewMessageDto dto);

    // 세션의 전체 대화 이력 (시간순) — 화면 렌더링 + AI 프롬프트 컨텍스트 구성에 공용으로 사용
    List<InterviewMessageDto> findBySessionId(@Param("sessionId") Long sessionId);
}
