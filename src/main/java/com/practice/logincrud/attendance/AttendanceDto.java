package com.practice.logincrud.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceDto {
    private Long id;
    private Long memberId;
    private LocalDate attendDate;
    private LocalDateTime createdAt;
}
