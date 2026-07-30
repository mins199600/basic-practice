package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDateTime;

// 자격증 "종류" 마스터 - 회원별 개인 기록(CertificationDto)과 문제은행이 공통으로 참조하는 표준 식별자.
@Data
public class CertificationCatalogDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
