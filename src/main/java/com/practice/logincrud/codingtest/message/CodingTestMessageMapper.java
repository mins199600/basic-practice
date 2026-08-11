package com.practice.logincrud.codingtest.message;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodingTestMessageMapper {

    void insert(CodingTestMessageDto dto);

    // 세션의 전체 메시지 이력 (문제 -> 답안 -> 첨삭 -> 모범답안) 시간순
    List<CodingTestMessageDto> findBySessionId(@Param("sessionId") Long sessionId);
}
