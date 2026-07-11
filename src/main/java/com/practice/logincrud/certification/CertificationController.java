package com.practice.logincrud.certification;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    // 자격증 목록
    @GetMapping("/certification")
    public String list(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");

        List<CertificationDto> certList = certificationService.getMyCertifications(memberId);

        model.addAttribute("certList", certList);
        return "certification/list";
    }

    // 등록 화면
    @GetMapping("/certification/create")
    public String createForm() {
        return "certification/create";
    }

    // 등록 처리
    @PostMapping("/certification/create")
    public String create(CertificationDto certificationDto, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        certificationDto.setMemberId(memberId);
        certificationService.save(certificationDto);
        return "redirect:/certification";
    }

    // 수정 화면
    @GetMapping("/certification/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        CertificationDto certification = certificationService.findById(id);

        if (certification == null || !certification.getMemberId().equals(memberId)) {
            return "redirect:/certification";
        }

        model.addAttribute("certification", certification);
        return "certification/edit";
    }

    // 수정 처리
    @PostMapping("/certification/edit/{id}")
    public String edit(@PathVariable Long id, CertificationDto certificationDto, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        CertificationDto certification = certificationService.findById(id);

        if (certification == null || !certification.getMemberId().equals(memberId)) {
            return "redirect:/certification";
        }

        certificationDto.setId(id);
        certificationService.update(certificationDto);
        return "redirect:/certification";
    }

    // 삭제
    @PostMapping("/certification/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        CertificationDto certification = certificationService.findById(id);

        if (certification == null || !certification.getMemberId().equals(memberId)) {
            return "redirect:/certification";
        }

        certificationService.delete(id);
        return "redirect:/certification";
    }
}
