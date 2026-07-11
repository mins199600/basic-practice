-- 자격증 관리 테이블
-- member_id는 기존 member.id(int(11))와 타입을 맞춰야 FK 생성이 됩니다.
CREATE TABLE certification (
    id          INT(11)      NOT NULL AUTO_INCREMENT,
    member_id   INT(11)      NOT NULL,
    cert_name   VARCHAR(100) NOT NULL,        -- 예: 정보처리기사, SQLD
    exam_date   DATE         NULL,            -- 시험 예정일 (없으면 NULL)
    status      VARCHAR(20)  NOT NULL DEFAULT '준비중',  -- 준비중 / 합격 / 불합격
    memo        VARCHAR(500) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_certification_member (member_id, deleted),
    CONSTRAINT fk_certification_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 출석(학습 체크인) 테이블
CREATE TABLE attendance (
    id          INT(11)  NOT NULL AUTO_INCREMENT,
    member_id   INT(11)  NOT NULL,
    attend_date DATE     NOT NULL,            -- 출석한 날짜 (하루 1건)
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_attendance_member (member_id, attend_date),
    UNIQUE KEY uq_attendance_member_date (member_id, attend_date),
    CONSTRAINT fk_attendance_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
