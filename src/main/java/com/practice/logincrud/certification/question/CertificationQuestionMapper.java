package com.practice.logincrud.certification.question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificationQuestionMapper {

    // 해당 자격증(카탈로그) (+ 과목)의 문제 id 목록을 id 오름차순으로 조회 (순차 출제용)
    // subjectId가 null이면 카탈로그 전체 문제를 대상으로 한다 (과목 분류가 없는 자격증과의 하위 호환).
    List<Long> findQuestionIdsInOrder(@Param("catalogId") Long catalogId,
                                       @Param("subjectId") Long subjectId);

    // 채점용 단건 조회 (정답 번호 / 해설 포함)
    CertificationQuestionDto findById(@Param("id") Long id);

    // 자격증(카탈로그) (+ 과목)에 문제가 하나라도 있는지 확인용
    int countByCertificationId(@Param("catalogId") Long catalogId,
                                @Param("subjectId") Long subjectId);

    // 관리자용 - 자격증(카탈로그)의 문제 목록(과목명 포함, id 오름차순, 페이지 단위). subjectId가 null이면 전체 과목.
    List<CertificationQuestionDto> findAllByCertificationId(@Param("catalogId") Long catalogId,
                                                              @Param("subjectId") Long subjectId,
                                                              @Param("offset") int offset,
                                                              @Param("pageSize") int pageSize);

    // 관리자용 - 자격증(카탈로그)의 문제 개수 (페이지네이션 총 페이지 수 계산용). subjectId가 null이면 전체 과목.
    int countAllByCertificationId(@Param("catalogId") Long catalogId,
                                   @Param("subjectId") Long subjectId);

    // 관리자용 - 문제 등록
    void insert(CertificationQuestionDto dto);

    // 관리자용 - 문제 수정
    void update(CertificationQuestionDto dto);

    // 관리자용 - 문제 삭제 (soft delete)
    void softDelete(@Param("id") Long id);
}
