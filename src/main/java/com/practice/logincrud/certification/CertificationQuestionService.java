package com.practice.logincrud.certification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationQuestionService {

    private final CertificationQuestionMapper certificationQuestionMapper;
    private final MemberQuestionStatMapper memberQuestionStatMapper;

    /**
     * "전체 → 오답만 → 또 오답만 → 100점" 방식 문제풀이의 1회차 큐를 구성할 때 쓰는 id 목록(id 오름차순).
     * subjectId가 null이면 자격증 전체 문제 대상 (과목 분류가 없는 자격증과의 하위 호환).
     */
    public List<Long> getQuestionIdsInOrder(Long catalogId, Long subjectId) {
        return certificationQuestionMapper.findQuestionIdsInOrder(catalogId, subjectId);
    }

    public CertificationQuestionDto findById(Long questionId) {
        return certificationQuestionMapper.findById(questionId);
    }

    public boolean hasQuestions(Long catalogId, Long subjectId) {
        return certificationQuestionMapper.countByCertificationId(catalogId, subjectId) > 0;
    }

    // 관리자용 - 자격증(카탈로그)의 문제 목록 (과목명 포함, 페이지 단위). subjectId가 null이면 전체 과목.
    public List<CertificationQuestionDto> getAllForAdmin(Long catalogId, Long subjectId, int offset, int pageSize) {
        return certificationQuestionMapper.findAllByCertificationId(catalogId, subjectId, offset, pageSize);
    }

    // 관리자용 - 자격증(카탈로그)의 문제 개수 (총 페이지 수 계산용). subjectId가 null이면 전체 과목.
    public int getTotalCountForAdmin(Long catalogId, Long subjectId) {
        return certificationQuestionMapper.countAllByCertificationId(catalogId, subjectId);
    }

    // 관리자용 - 문제 등록
    public void create(CertificationQuestionDto dto) {
        validate(dto);
        certificationQuestionMapper.insert(dto);
        log.info("문제 등록 catalogId={} subjectId={}", dto.getCatalogId(), dto.getSubjectId());
    }

    // 관리자용 - 문제 수정
    public void update(CertificationQuestionDto dto) {
        validate(dto);
        certificationQuestionMapper.update(dto);
        log.info("문제 수정 id={}", dto.getId());
    }

    // 관리자용 - 문제 삭제 (soft delete)
    public void delete(Long id) {
        certificationQuestionMapper.softDelete(id);
        log.info("문제 삭제 id={}", id);
    }

    // 문제 등록/수정 공통 입력값 검증 - 잘못된 데이터가 그대로 저장되어 퀴즈 화면에서 깨지는 것을 미리 막는다.
    private void validate(CertificationQuestionDto dto) {
        if (dto.getCatalogId() == null) {
            throw new IllegalArgumentException("자격증을 선택해주세요.");
        }
        if (dto.getQuestionText() == null || dto.getQuestionText().isBlank()) {
            throw new IllegalArgumentException("문제 내용을 입력해주세요.");
        }
        if (isBlank(dto.getChoice1()) || isBlank(dto.getChoice2()) || isBlank(dto.getChoice3()) || isBlank(dto.getChoice4())) {
            throw new IllegalArgumentException("선택지 4개를 모두 입력해주세요.");
        }
        if (dto.getAnswerNo() == null || dto.getAnswerNo() < 1 || dto.getAnswerNo() > 4) {
            throw new IllegalArgumentException("정답 번호는 1~4 중에서 선택해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 채점 결과를 회원-문제 통계에 반영한다.
     * 정답: weight -1 (최소 1) / 오답: weight +2
     */
    public void recordAnswer(Long memberId, Long questionId, boolean correct) {
        int weightDelta = correct ? -1 : 2;
        int correctInc = correct ? 1 : 0;
        int wrongInc = correct ? 0 : 1;

        memberQuestionStatMapper.upsertStat(memberId, questionId, weightDelta, correctInc, wrongInc);

        log.info("문제 통계 갱신 memberId={} questionId={} correct={} weightDelta={}",
                memberId, questionId, correct, weightDelta);
    }
}
