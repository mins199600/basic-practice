package com.practice.logincrud.interview;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewSessionMapper {

    void insert(InterviewSessionDto dto);

    InterviewSessionDto findById(@Param("id") Long id);

    List<InterviewSessionDto> findByProjectId(@Param("projectId") Long projectId);

    void incrementQuestionCount(@Param("id") Long id);

    void endSession(@Param("id") Long id);
}
