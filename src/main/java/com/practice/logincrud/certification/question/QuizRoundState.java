package com.practice.logincrud.certification.question;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * "전체 → 오답만 → 또 오답만 → 100점" 방식 문제풀이의 진행 상태.
 * DB가 아니라 HttpSession에 저장해서 회차 진행 중에만 유지한다(로그아웃/세션만료 시 처음부터 다시 시작).
 *
 * 흐름:
 *  1회차: remainingQueue = 과목의 전체 문제 id (id 오름차순)
 *  문제를 하나 보여줄 때마다 remainingQueue 맨 앞을 꺼내 쓰고, 틀리면 wrongThisRound에 쌓는다.
 *  remainingQueue가 다 떨어졌을 때 wrongThisRound가 비어있으면 100점(완료), 아니면
 *  wrongThisRound를 다음 회차의 remainingQueue로 삼아 roundNumber를 1 올리고 이어간다.
 */
@Data
@AllArgsConstructor
public class QuizRoundState implements Serializable {
    private int roundNumber;
    private List<Long> remainingQueue;
    private List<Long> wrongThisRound;
    private int totalInRound;
}
