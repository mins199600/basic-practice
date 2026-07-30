package com.practice.logincrud.certification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificationCatalogMapper {

    CertificationCatalogDto findByName(@Param("name") String name);

    CertificationCatalogDto findById(@Param("id") Long id);

    // 관리자 드롭다운용 - 이름순
    List<CertificationCatalogDto> findAll();

    void insert(CertificationCatalogDto dto);
}
