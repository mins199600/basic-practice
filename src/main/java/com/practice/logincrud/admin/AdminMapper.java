package com.practice.logincrud.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    //회원가입
    int insertAdmin(AdminDto adminDto);

    //로그인
    AdminDto adminLogin(String email);

    //로그인 시도용 조회 - 승인대기 상태까지 포함해서 "비번 틀림"과 "승인 대기"를 구분한다
    AdminDto findAccountForLogin(String email);

    //아이디 찾기 - 사번 조회
    String matchedEmail(String empNo);

    //비밀번호 찾기 - 이메일 + 닉네임으로 계정 확인
    AdminDto findByEmailAndNickname(@Param("email") String email, @Param("nickname") String nickname);

    //비밀번호 변경
    void updatePassword(@Param("email") String email, @Param("password") String password);

    //관리자 가입 시 이미 사용 중인 이메일인지 확인
    int existsByEmail(String email);

    //승인 대기 중인 관리자 목록
    java.util.List<AdminDto> findPendingAdmins();

    //관리자 승인 - 영향받은 row 수(0이면 이미 처리됐거나 대상 없음)
    int approveAdmin(@Param("id") Long id, @Param("empNo") String empNo);

    //관리자 승인 거부(소프트 삭제) - 영향받은 row 수
    int rejectAdmin(Long id);

    //최고관리자 이메일 목록 - 승인 요청 알림 발송용
    java.util.List<String> findSuperAdminEmails();

    //올해 마지막 사번 조회
    Integer getLastEmpNoSeq(String year);
}
