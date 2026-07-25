package com.practice.logincrud.admin;

import com.practice.logincrud.board.PageDto;
import com.practice.logincrud.certification.CertificationDto;
import com.practice.logincrud.certification.CertificationQuestionDto;
import com.practice.logincrud.certification.CertificationQuestionService;
import com.practice.logincrud.certification.CertificationService;
import com.practice.logincrud.certification.SubjectDto;
import com.practice.logincrud.certification.SubjectService;
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
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminCertificationQuestionController {

    private final CertificationQuestionService certificationQuestionService;
    private final CertificationService certificationService;
    private final SubjectService subjectService;

    // 문제 목록 - 상단에서 자격증을 선택하고, 과목 탭으로 좁혀서 페이지 단위로 보여준다.
    @GetMapping("/admin/certification-questions")
    public String list(@RequestParam(required = false) Long certificationId,
                        @RequestParam(required = false) Long subjectId,
                        PageDto pageDto, Model model) {
        List<CertificationDto> certList = certificationService.getAllForAdmin();

        if (certificationId == null && !certList.isEmpty()) {
            certificationId = certList.get(0).getId();
        }

        List<SubjectDto> subjectList = certificationId != null
                ? subjectService.getSubjects(certificationId)
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
        if (certificationId != null) {
            questionList = certificationQuestionService.getAllForAdmin(
                    certificationId, subjectId, pageDto.getOffset(), pageDto.getPageSize());
            totalCount = certificationQuestionService.getTotalCountForAdmin(certificationId, subjectId);
        }
        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageSize());

        model.addAttribute("certList", certList);
        model.addAttribute("selectedCertificationId", certificationId);
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
    public String newForm(@RequestParam Long certificationId, Model model) {
        model.addAttribute("question", new CertificationQuestionDto());
        model.addAttribute("certList", certificationService.getAllForAdmin());
        model.addAttribute("selectedCertificationId", certificationId);
        model.addAttribute("subjectList", subjectService.getSubjects(certificationId));
        return "admin/certification-question/create";
    }

    // 문제 등록 처리 - 검증 실패 시 입력값을 유지한 채 같은 폼을 다시 보여준다.
    @PostMapping("/admin/certification-questions/new")
    public String create(CertificationQuestionDto question, Model model, RedirectAttributes redirectAttributes) {
        try {
            certificationQuestionService.create(question);
            redirectAttributes.addFlashAttribute("message", "문제가 등록되었습니다.");
            return "redirect:/admin/certification-questions?certificationId=" + question.getCertificationId();
        } catch (IllegalArgumentException e) {
            log.warn("문제 등록 실패 certificationId={} reason={}", question.getCertificationId(), e.getMessage());
            model.addAttribute("question", question);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("certList", certificationService.getAllForAdmin());
            model.addAttribute("selectedCertificationId", question.getCertificationId());
            model.addAttribute("subjectList", question.getCertificationId() != null
                    ? subjectService.getSubjects(question.getCertificationId()) : List.of());
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
        model.addAttribute("certList", certificationService.getAllForAdmin());
        model.addAttribute("selectedCertificationId", question.getCertificationId());
        model.addAttribute("subjectList", subjectService.getSubjects(question.getCertificationId()));
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
            return "redirect:/admin/certification-questions?certificationId=" + question.getCertificationId() + subjectParam;
        } catch (IllegalArgumentException e) {
            log.warn("문제 수정 실패 id={} reason={}", id, e.getMessage());
            model.addAttribute("question", question);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("certList", certificationService.getAllForAdmin());
            model.addAttribute("selectedCertificationId", question.getCertificationId());
            model.addAttribute("subjectList", question.getCertificationId() != null
                    ? subjectService.getSubjects(question.getCertificationId()) : List.of());
            return "admin/certification-question/edit";
        }
    }

    // 문제 삭제 (soft delete)
    @PostMapping("/admin/certification-questions/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long certificationId,
                          @RequestParam(required = false) Long subjectId,
                          RedirectAttributes redirectAttributes) {
        certificationQuestionService.delete(id);
        redirectAttributes.addFlashAttribute("message", "문제가 삭제되었습니다.");
        String subjectParam = subjectId != null ? "&subjectId=" + subjectId : "";
        return "redirect:/admin/certification-questions?certificationId=" + certificationId + subjectParam;
    }
}
