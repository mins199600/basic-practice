package com.practice.logincrud.certification;

import lombok.Data;

@Data
public class SubjectDto {
    private Long id;
    private Long catalogId;
    private String name;
    private Integer displayOrder;

    // 과목별 문제 개수 (문제 없는 과목은 화면에서 비활성 처리하기 위한 용도)
    private Integer questionCount;
}
