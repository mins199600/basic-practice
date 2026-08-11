package com.practice.logincrud.admin;

import com.practice.logincrud.board.PageDto;
import com.practice.logincrud.certification.catalog.CertificationCatalogDto;
import com.practice.logincrud.certification.catalog.CertificationCatalogService;
import com.practice.logincrud.certification.question.CertificationQuestionDto;
import com.practice.logincrud.certification.question.CertificationQuestionService;
import com.practice.logincrud.certification.subject.SubjectDto;
import com.practice.logincrud.certification.subject.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 관리자 전용 - 자격증 문제(문제은행) 관리.
 * /admin/** 경로는 WebConfig의 AdminInterceptor가 이미 ADMIN 권한을 검사하므로,
 * 이 컨트롤러에서는 별도의 권한 체크 없이 편집 기능만 구현한다.
 * (자격증 문제풀이용 CertificationQuizController와는 별개의 관리자 화면)
 *
 * 2026-07-28: 드롭다운 소스를 "회원 개인 자격증 기록 전체"에서 "자격증 카탈로그(종류) 목록"으로 변경.
 * 예전엔 다른 회원의 개인 기록(certification) id를 그대로 문제은행 식별자로 썼는데,
 * 그게 이번 리팩터링으로 고친 버그의 원인이었다 - 관리자 화면도 같은 원인을 갖고 있었음.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminCertificationQuestionController {

    private final CertificationQuestionService certificationQuestionService;
    private final CertificationCatalogService certificationCatalogService;
    private final SubjectService subjectService;

    // 문제 목록 - 상단에서 자격증(카탈로그)을 선택하고, 과목 탭으로 좁혀서 페이지 단위로 보여준다.
    @GetMapping("/admin/certification-questions")
    public String list(@RequestParam(required = false) Long catalogId,
                        @RequestParam(required = false) Long subjectId,
                        PageDto pageDto, Model model) {
        List<CertificationCatalogDto> catalogList = certificationCatalogService.getAll();

        if (catalogId == null && !catalogList.isEmpty()) {
            catalogId = catalogList.get(0).getId();
        }

        List<SubjectDto> subjectList = catalogId != null
                ? subjectService.getSubjects(catalogId)
                : List.of();

        // 과목 탭에 없는(다른 자격증의) subjectId가 URL로 들어오면 무시하고 "전체"로 되돌린다.
        Long requestedSubjectId = subjectId;
        boolean subjectBelongsToCert = requestedSubjectId != null
                && subjectList.stream().anyMatch(s -> s.getId().equals(requestedSubjectId));
        if (!subjectBelongsToCert) {
            subjectId = null;
        }

        List<CertificationQuestionDto> questionList = List.of();
        int totalCount = 0;
        if (catalogId != null) {
            questionList = certificationQuestionService.getAllForAdmin(
                    catalogId, subjectId, pageDto.getOffset(), pageDto.getPageSize());
            totalCount = certificationQuestionService.getTotalCountForAdmin(catalogId, subjectId);
        }
        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageSize());

        model.addAttribute("certList", catalogList);
        model.addAttribute("selectedCatalogId", catalogId);
        model.addAttribute("subjectList", subjectList);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("questionList", questionList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", pageDto.getPage());
        model.addAttribute("totalPage", totalPage);
        return "admin/certification-question/list";
    }

    // 문제 등록 폼
    @GetMapping("/admin/certification-questions/new")
    public String newForm(@RequestParam Long catalogId, Model model) {
        model.addAttribute("question", new CertificationQuestionDto());
        model.addAttribute("certList", certificationCatalogService.getAll());
        model.addAttribute("selectedCatalogId", catalogId);
        model.addAttribute("subjectList", subjectService.getSubjects(catalogId));
        return "admin/certification-question/create";
    }

    // 문제 등록 처리 - 검증 실패 시 입력값을 유지한 채 같은 폼을 다시 보여준다.
    @PostMapping("/admin/certification-questions/new")
    public String create(CertificationQuestionDto question, Model model, RedirectAttributes redirectAttributes) {
        try {
            certificationQuestionService.create(question);
            redirectAttributes.addFlashAttribute("message", "문제가 등록되었습니다.");
            return "redirect:/admin/certification-questions?catalogId=" + question.getCatalogId();
        } catch (IllegalArgumentException e) {
            log.warn("문제 등록 실패 catalogId={} reason={}", question.getCatalogId(), e.getMessage());
            model.addAttribute("question", question);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("certList", certificationCatalogService.getAll());
            model.addAttribute("selectedCatalogId", question.getCatalogId());
            model.addAttribute("subjectList", question.getCatalogId() != null
                    ? subjectService.getSubjects(question.getCatalogId()) : List.of());
            return "admin/certification-question/create";
        }
    }

    // 문제 수정 폼
    @GetMapping("/admin/certification-questions/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CertificationQuestionDto question = certificationQuestionService.findById(id);
        if (question == null) {
            return "redirect:/admin/certification-questions";
        }

        model.addAttribute("question", question);
        model.addAttribute("certList", certificationCatalogService.getAll());
        model.addAttribute("selectedCatalogId", question.getCatalogId());
        model.addAttribute("subjectList", subjectService.getSubjects(question.getCatalogId()));
        return "admin/certification-question/edit";
    }

    // 문제 수정 처리
    @PostMapping("/admin/certification-questions/{id}/edit")
    public String edit(@PathVariable Long id, CertificationQuestionDto question, Model model,
                        RedirectAttributes redirectAttributes) {
        question.setId(id); // 경로의 id를 그대로 사용 - 폼에 숨겨진 값이 조작되어도 다른 문제가 수정되지 않도록 방어

        try {
            certificationQuestionService.update(question);
            redirectAttributes.addFlashAttribute("message", "문제가 수정되었습니다.");
            String subjectParam = question.getSubjectId() != null ? "&subjectId=" + question.getSubjectId() : "";
            return "redirect:/admin/certification-questions?catalogId=" + question.getCatalogId() + subjectParam;
        } catch (IllegalArgumentException e) {
            log.warn("문제 수정 실패 id={} reason={}", id, e.getMessage());
            model.addAttribute("question", question);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("certList", certificationCatalogService.getAll());
            model.addAttribute("selectedCatalogId", question.getCatalogId());
            model.addAttribute("subjectList", question.getCatalogId() != null
                    ? subjectService.getSubjects(question.getCatalogId()) : List.of());
            return "admin/certification-question/edit";
        }
    }

    // 문제 삭제 (soft delete)
    @PostMapping("/admin/certification-questions/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long catalogId,
                          @RequestParam(required = false) Long subjectId,
                          RedirectAttributes redirectAttributes) {
        certificationQuestionService.delete(id);
        redirectAttributes.addFlashAttribute("message", "문제가 삭제되었습니다.");
        String subjectParam = subjectId != null ? "&subjectId=" + subjectId : "";
        return "redirect:/admin/certification-questions?catalogId=" + catalogId + subjectParam;
    }
}
