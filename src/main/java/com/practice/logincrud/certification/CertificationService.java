package com.practice.logincrud.certification;

import com.practice.logincrud.certification.catalog.CertificationCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationMapper certificationMapper;
    private final CertificationCatalogService certificationCatalogService;

    public List<CertificationDto> getMyCertifications(Long memberId) {
        return certificationMapper.findByMemberId(memberId);
    }

    public CertificationDto findById(Long id) {
        return certificationMapper.findById(id);
    }

    // 자격증명을 카탈로그(자격증 종류 마스터)와 이름 기준으로 매칭/자동 생성해서 연결한다.
    // 이렇게 해야 서로 다른 회원이 같은 이름으로 등록해도 같은 문제은행을 볼 수 있다.
    public void save(CertificationDto certificationDto) {
        if (certificationDto.getStatus() == null || certificationDto.getStatus().isBlank()) {
            certificationDto.setStatus("준비중");
        }
        certificationDto.setCatalogId(resolveCatalogId(certificationDto.getCertName()));
        certificationMapper.insert(certificationDto);
    }

    public void update(CertificationDto certificationDto) {
        certificationDto.setCatalogId(resolveCatalogId(certificationDto.getCertName()));
        certificationMapper.update(certificationDto);
    }

    private Long resolveCatalogId(String certName) {
        if (certName == null || certName.isBlank()) {
            return null;
        }
        return certificationCatalogService.getOrCreateByName(certName.trim()).getId();
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
