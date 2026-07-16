package com.practice.logincrud.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectMapper subjectMapper;

    public List<SubjectDto> getSubjects(Long certificationId) {
        return subjectMapper.findByCertificationId(certificationId);
    }
}
