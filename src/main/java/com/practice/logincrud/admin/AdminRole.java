package com.practice.logincrud.admin;

/**
 * member.role 컬럼에 저장되는 값과 1:1로 매핑되는 관리자 권한 등급.
 * "ADMIN"/"2" 같은 문자열 리터럴을 여러 파일에 흩어놓고 비교하면
 * (세션에 실수로 다른 타입이 들어오는 경우 등) 비교가 조용히 실패할 수 있어
 * 원시 값 파싱과 비교 로직을 이 enum 하나로 모은다.
 */
public enum AdminRole {
    USER("USER"),
    PENDING_ADMIN("PENDING_ADMIN"),
    ADMIN("ADMIN"),
    SUPER_ADMIN("2");

    private final String value;

    AdminRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    //세션/DB에서 꺼낸 원시 값을 enum으로 변환. String이 아닌 값(Integer 등)이 들어와도 String.valueOf로 방어적으로 비교한다.
    public static AdminRole fromRaw(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw);
        for (AdminRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

    //관리자 페이지(/admin/**) 접근 가능 여부 - 승인된 관리자 + 최고관리자
    public boolean isAdminOrAbove() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
