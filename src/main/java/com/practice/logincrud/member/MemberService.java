package com.practice.logincrud.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class MemberService {

    @Autowired
    MemberMapper memberMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 5MB (application.yml 설정과 별개로 서비스 레벨에서도 재검증)

    //로그인
    public UserDto userLogin(String email, String password) {
        UserDto user = memberMapper.findUserLogin(email);

        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;  // 로그인 성공 → user 반환
        }

        return null;  // 비밀번호 불일치
    }

    //회원가입
    public boolean join(String email, String password, String nickName) {
        int count = memberMapper.countByEmail(email);
        if (count > 0) {
            log.warn("join 실패 - 중복 이메일 email={}", email);
            return false;
        }
        UserDto user = new UserDto();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickName(nickName);
        memberMapper.insertMember(user);

        log.info("join 성공 email={}", email);
        return true;

    }

    // 회원정보 조회
    public UserDto findUserByEmail(String email) {
        return memberMapper.findUserByEmail(email);
    }

    // 회원정보 수정
    public boolean updateUser(String oldEmail, UserDto userDto) {

        String newEmail = userDto.getEmail();

        // 이메일을 변경한 경우에만 중복 검사
        if (!oldEmail.equals(newEmail)) {
            int count = memberMapper.countByEmail(newEmail);

            if (count > 0) {
                return false;
            }
        }

        String password = userDto.getPassword();

        if (password != null && !password.trim().isEmpty()) {
            userDto.setPassword(passwordEncoder.encode(password));
        } else {
            userDto.setPassword(null);
        }

        memberMapper.updateUser(oldEmail, userDto);

        return true;
    }

    // 회원 삭제
    public void deleteUser(String email) {
        memberMapper.deleteUser(email);
        log.info("회원 soft delete 완료");
    }

    //전체 회원 수
    public int getTotalMemberCount() {
        return memberMapper.getTotalMemberCount();
    }

    public List<UserDto> getAllMembers() {
        return memberMapper.getAllMembers();
    }

    public List<UserDto> getRecentMembers() {
        return memberMapper.getRecentMembers();
    }

    //비밀번호 재설정
    public void resetPassword(String email, String newPassword) {
        memberMapper.updatePassword(email, passwordEncoder.encode(newPassword));
        log.info("비밀번호 재설정 완료 email={}", email);
    }

    /**
     * 프로필 사진 업로드.
     * 검증(형식/용량) → 파일시스템 저장 → DB의 profile_image 갱신 → 기존 파일 삭제(best effort) 순서로 처리한다.
     * 실패 시 IllegalArgumentException(사용자 입력 문제) 또는 RuntimeException(서버/IO 문제)을 던지고,
     * 호출부(Controller)에서 구분해서 사용자에게 다른 메시지를 보여준다.
     */
    public String uploadProfileImage(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지를 선택해주세요.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("jpg, png, webp, gif 형식의 이미지만 업로드할 수 있습니다.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("이미지 용량은 5MB를 넘을 수 없습니다.");
        }

        UserDto user = memberMapper.findUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        try {
            Path targetDir = Paths.get(uploadDir, "profile").toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            String filename = "member_" + user.getId() + "_" + UUID.randomUUID() + extractExtension(file.getOriginalFilename(), contentType);
            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath);

            String imageUrl = "/uploads/profile/" + filename;
            memberMapper.updateProfileImage(email, imageUrl);

            deleteOldImage(uploadDir, user.getProfileImage());

            log.info("프로필 이미지 업로드 완료 email={} file={}", email, filename);
            return imageUrl;

        } catch (IOException e) {
            log.error("프로필 이미지 저장 실패 email={}", email, e);
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    // 기존 프로필 이미지 파일 삭제. 실패해도 전체 흐름을 막지 않고 로그만 남긴다(디스크 정리는 부가 기능일 뿐).
    private void deleteOldImage(String uploadDir, String oldImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank()) {
            return;
        }
        try {
            String relativePath = oldImageUrl.replaceFirst("^/uploads/", "");
            Path oldPath = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(oldPath);
        } catch (IOException e) {
            log.warn("기존 프로필 이미지 삭제 실패(무시하고 진행) path={}", oldImageUrl, e);
        }
    }

    // 원본 파일명의 확장자를 우선 사용하고, 없거나 이상하면 content-type 기준으로 대체한다.
    private String extractExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            if (ext.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

}