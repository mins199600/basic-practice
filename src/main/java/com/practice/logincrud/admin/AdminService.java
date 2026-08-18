package com.practice.logincrud.admin;

import com.practice.logincrud.member.UserDto;
import com.practice.logincrud.member.email.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    //관리자 가입 신청 - 바로 ADMIN이 되지 않고 승인 대기 상태로 저장
    public boolean joinAdmin(String email, String password, String nickName) {

        AdminDto adminDto = new AdminDto();
        adminDto.setEmail(email);
        adminDto.setPassword(passwordEncoder.encode(password));
        adminDto.setNickname(nickName);
        adminDto.setRole(AdminRole.PENDING_ADMIN.getValue());

        int count = adminMapper.insertAdmin(adminDto);
        if (count != 1) {
            return false;
        }

        notifySuperAdmins(email);
        return true;
    }

    //가입 신청이 들어왔음을 최고관리자(role=2) 전원에게 메일로 알림
    private void notifySuperAdmins(String requesterEmail) {
        List<String> superAdminEmails = adminMapper.findSuperAdminEmails();
        if (superAdminEmails.isEmpty()) {
            log.warn("관리자 승인 요청 알림 실패 - 등록된 최고관리자가 없음 requesterEmail={}", requesterEmail);
            return;
        }
        for (String superAdminEmail : superAdminEmails) {
            emailSender.sendAdminApprovalRequest(superAdminEmail, requesterEmail);
        }
    }

    //이미 사용 중인 이메일인지 확인 (인증코드 발송 전 차단용)
    public boolean isEmailTaken(String email) {
        return adminMapper.existsByEmail(email) > 0;
    }

    //승인 대기 중인 관리자 목록 (최고관리자 전용)
    public List<AdminDto> findPendingAdmins() {
        return adminMapper.findPendingAdmins();
    }

    //관리자 승인 (최고관리자 전용) - 대상이 이미 처리됐으면 false
    public boolean approveAdmin(Long id) {
        return adminMapper.approveAdmin(id) == 1;
    }

    //관리자 승인 거부 (최고관리자 전용) - 대상이 이미 처리됐으면 false
    public boolean rejectAdmin(Long id) {
        return adminMapper.rejectAdmin(id) == 1;
    }

    //로그인 - 승인대기(PENDING_ADMIN) 상태는 비번이 맞아도 로그인시키지 않고 명확한 예외로 알린다
    public AdminDto adminAccess(String email, String password) {
        AdminDto admin = adminMapper.findAccountForLogin(email);
        if (admin == null) {
            log.info("관리자 계정 조회 실패");
            return null;
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.info("관리자 비밀번호 불일치");
            return null;
        }

        AdminRole role = AdminRole.fromRaw(admin.getRole());
        if (role == null || !role.isAdminOrAbove()) {
            // PENDING_ADMIN 등 아직 승인되지 않은 상태 - 비번은 맞았으니 "승인 대기 중"임을 명확히 알린다
            log.info("관리자 로그인 차단 - 승인 대기 상태 email={}", email);
            throw new IllegalStateException("승인 대기 중인 계정입니다. 최고관리자 승인 후 로그인할 수 있습니다.");
        }

        log.info("관리자 로그인 성공 email={}, role={}", admin.getEmail(), admin.getRole());
        return admin;
    }

    //이메일 인증코드 확인 후 세션에 채울 관리자 정보 재조회 (비번은 로그인 1단계에서 이미 검증됨)
    public AdminDto findByEmail(String email) {
        return adminMapper.adminLogin(email);
    }

    //아이디 찾기
    public String findEmail(String nickname) {
        return adminMapper.findEmailByNickname(nickname);
    }

    //비밀번호 찾기 - 본인 확인
    public boolean verifyAdmin(String email, String nickname) {
        AdminDto admin = adminMapper.findByEmailAndNickname(email, nickname);
        return admin != null;
    }

    //비밀번호 변경
    public void updatePassword(String email, String newPassword) {
        String encoded = passwordEncoder.encode(newPassword);
        adminMapper.updatePassword(email, encoded);
    }
}
