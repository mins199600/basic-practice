package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificationDto {
    private Long id;
    private Long memberId;
    private Long catalogId;    // 자격증 종류 마스터(certification_catalog) 참조 - 이름 매칭으로 자동 세팅됨, 느슨한 연결(FK 없음)이라 null 가능
    private String certName;   // 정보처리기사, SQLD 등
    private LocalDate examDate;
    private String status;     // 준비중 / 합격 / 불합격
    private String memo;
    private LocalDateTime createdAt;
}
