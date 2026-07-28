package com.practice.logincrud.certification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificationMapper {

    // 내 자격증 목록 (최신순)
    List<CertificationDto> findByMemberId(@Param("memberId") Long memberId);

    // 단건 조회
    CertificationDto findById(@Param("id") Long id);

    // 등록
    void insert(CertificationDto certificationDto);

    // 수정
    void update(CertificationDto certificationDto);

    // 삭제 (soft delete)
    void delete(@Param("id") Long id);

    // 전체 개수 (본인 것 중 삭제 제외)
    int countByMemberId(@Param("memberId") Long memberId);

    // 합격 개수
    int countPassedByMemberId(@Param("memberId") Long memberId);

    // 관리자용 - 회원 구분 없이 전체 자격증(문제은행) 목록 (id 오름차순)
    List<CertificationDto> findAllForAdmin();
}
