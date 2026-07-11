package com.practice.logincrud.certification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationMapper certificationMapper;

    public List<CertificationDto> getMyCertifications(Long memberId) {
        return certificationMapper.findByMemberId(memberId);
    }

    public CertificationDto findById(Long id) {
        return certificationMapper.findById(id);
    }

    public void save(CertificationDto certificationDto) {
        if (certificationDto.getStatus() == null || certificationDto.getStatus().isBlank()) {
            certificationDto.setStatus("준비중");
        }
        certificationMapper.insert(certificationDto);
    }

    public void update(CertificationDto certificationDto) {
        certificationMapper.update(certificationDto);
    }

    public void delete(Long id) {
        certificationMapper.delete(id);
    }

    // 대시보드용 - 합격률(%) 계산. 등록된 자격증이 없으면 0
    public int getPassRate(Long memberId) {
        int total = certificationMapper.countByMemberId(memberId);
        if (total == 0) {
            return 0;
        }
        int passed = certificationMapper.countPassedByMemberId(memberId);
        return (int) Math.round(passed * 100.0 / total);
    }

    public int getTotalCount(Long memberId) {
        return certificationMapper.countByMemberId(memberId);
    }
}
