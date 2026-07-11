package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificationDto {
    private Long id;
    private Long memberId;
    private String certName;   // 정보처리기사, SQLD 등
    private LocalDate examDate;
    private String status;     // 준비중 / 합격 / 불합격
    private String memo;
    private LocalDateTime createdAt;
}
