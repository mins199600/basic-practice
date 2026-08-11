package com.practice.logincrud.certification.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationCatalogService {

    private final CertificationCatalogMapper certificationCatalogMapper;

    // 이름으로 카탈로그를 찾고, 없으면 새로 만든다.
    // 동시에 같은 이름으로 처음 등록되는 경우(레이스 컨디션) unique 제약 위반이 나면,
    // 다른 트랜잭션이 먼저 만든 것이므로 다시 조회해서 반환한다.
    @Transactional
    public CertificationCatalogDto getOrCreateByName(String name) {
        CertificationCatalogDto existing = certificationCatalogMapper.findByName(name);
        if (existing != null) {
            return existing;
        }

        CertificationCatalogDto dto = new CertificationCatalogDto();
        dto.setName(name);
        try {
            certificationCatalogMapper.insert(dto);
            return dto;
        } catch (DuplicateKeyException e) {
            log.info("자격증 카탈로그 동시 생성 감지 - 재조회 name={}", name);
            CertificationCatalogDto winner = certificationCatalogMapper.findByName(name);
            if (winner == null) {
                throw e;
            }
            return winner;
        }
    }

    public List<CertificationCatalogDto> getAll() {
        return certificationCatalogMapper.findAll();
    }

    public CertificationCatalogDto findById(Long id) {
        return certificationCatalogMapper.findById(id);
    }
}
