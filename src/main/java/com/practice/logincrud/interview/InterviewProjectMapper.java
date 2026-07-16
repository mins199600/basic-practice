package com.practice.logincrud.interview;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewProjectMapper {

    void insert(InterviewProjectDto dto);

    InterviewProjectDto findById(@Param("id") Long id);

    // 로그인한 회원이 등록한 프로젝트 목록 (최신순)
    List<InterviewProjectDto> findByMemberId(@Param("memberId") Long memberId);

    // 동일 회원이 같은 저장소를 중복 등록하지 않았는지 확인용
    int countByMemberIdAndRepoUrl(@Param("memberId") Long memberId, @Param("repoUrl") String repoUrl);
}
