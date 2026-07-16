package com.practice.logincrud.certification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubjectMapper {

    // 자격증에 등록된 과목(파트) 목록 조회
    List<SubjectDto> findByCertificationId(@Param("certificationId") Long certificationId);
}
