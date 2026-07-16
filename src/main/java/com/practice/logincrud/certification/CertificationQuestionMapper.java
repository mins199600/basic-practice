package com.practice.logincrud.certification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificationQuestionMapper {

    // 해당 자격증(+ 과목 선택 시 해당 과목)의 전체 문제 + 이 회원 기준 가중치(없으면 기본 1)를 함께 조회
    // subjectId가 null이면 자격증 전체 문제를 대상으로 한다 (과목 분류가 없는 자격증과의 하위 호환).
    List<CertificationQuestionDto> findCandidatesWithWeight(@Param("certificationId") Long certificationId,
                                                             @Param("memberId") Long memberId,
                                                             @Param("subjectId") Long subjectId);

    // 채점용 단건 조회 (정답 번호 / 해설 포함)
    CertificationQuestionDto findById(@Param("id") Long id);

    // 자격증(+ 과목)에 등록된 문제 개수 (문제 없음 안내용)
    int countByCertificationId(@Param("certificationId") Long certificationId,
                                @Param("subjectId") Long subjectId);
}
