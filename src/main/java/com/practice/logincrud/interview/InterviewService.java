package com.practice.logincrud.interview;

import com.practice.logincrud.interview.github.GitHubRepoInfo;
import com.practice.logincrud.interview.github.GitHubService;
import com.practice.logincrud.interview.message.InterviewMessageDto;
import com.practice.logincrud.interview.message.InterviewMessageMapper;
import com.practice.logincrud.interview.project.InterviewProjectDto;
import com.practice.logincrud.interview.project.InterviewProjectMapper;
import com.practice.logincrud.interview.session.InterviewSessionDto;
import com.practice.logincrud.interview.session.InterviewSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewProjectMapper interviewProjectMapper;
    private final InterviewSessionMapper interviewSessionMapper;
    private final InterviewMessageMapper interviewMessageMapper;
    private final GitHubService gitHubService;
    private final ClaudeAiService claudeAiService;

    // 답변 이 개수만큼 쌓이면 "면접 준비율" 100%로 간주한다 (모의면접 2회 x 질문 5개 수준).
    private static final int PREP_TARGET_ANSWERS = 10;

    public List<InterviewProjectDto> getMyProjects(Long memberId) {
        return interviewProjectMapper.findByMemberId(memberId);
    }

    /**
     * GitHub URL 하나만 받아 저장소 정보를 조회하고 프로젝트로 등록한다.
     * 같은 회원이 같은 저장소를 중복 등록하지 않도록 막는다.
     */
    public InterviewProjectDto registerProject(Long memberId, String repoUrl) {
        String[] parsed = gitHubService.parseRepoUrl(repoUrl);
        String owner = parsed[0];
        String repo = parsed[1];

        if (interviewProjectMapper.countByMemberIdAndRepoUrl(memberId, repoUrl) > 0) {
            throw new InterviewIntegrationException("이미 등록한 저장소입니다.");
        }

        GitHubRepoInfo repoInfo = gitHubService.fetchRepoInfo(owner, repo);

        InterviewProjectDto dto = new InterviewProjectDto();
        dto.setMemberId(memberId);
        dto.setRepoUrl(repoUrl.trim());
        dto.setRepoOwner(repoInfo.getOwner());
        dto.setRepoName(repoInfo.getName());
        dto.setDescription(repoInfo.getDescription());
        dto.setPrimaryLanguage(repoInfo.getPrimaryLanguage());
        dto.setReadmeText(repoInfo.getReadmeText());

        interviewProjectMapper.insert(dto);
        log.info("프로젝트 등록 memberId={} repo={}/{}", memberId, owner, repo);
        return dto;
    }

    /** 소유권 검증까지 포함한 프로젝트 단건 조회. 다른 회원의 프로젝트면 null을 반환한다. */
    public InterviewProjectDto getOwnedProject(Long projectId, Long memberId) {
        InterviewProjectDto project = interviewProjectMapper.findById(projectId);
        if (project == null || !project.getMemberId().equals(memberId)) {
            return null;
        }
        return project;
    }

    public List<InterviewSessionDto> getSessions(Long projectId) {
        return interviewSessionMapper.findByProjectId(projectId);
    }

    /** 새 면접 세션을 시작하고, AI에게 첫 질문을 받아 저장한다. */
    public InterviewSessionDto startSession(Long projectId, Long memberId) {
        InterviewProjectDto project = getOwnedProject(projectId, memberId);
        if (project == null) {
            throw new InterviewIntegrationException("존재하지 않거나 접근 권한이 없는 프로젝트입니다.");
        }

        InterviewSessionDto session = new InterviewSessionDto();
        session.setProjectId(projectId);
        session.setMemberId(memberId);
        interviewSessionMapper.insert(session);

        String firstQuestion = claudeAiService.startInterview(toRepoInfo(project));
        saveAiMessage(session.getId(), "QUESTION", firstQuestion);
        interviewSessionMapper.incrementQuestionCount(session.getId());

        log.info("면접 세션 시작 sessionId={} projectId={} memberId={}", session.getId(), projectId, memberId);
        return interviewSessionMapper.findById(session.getId());
    }

    /** 소유권 검증까지 포함한 세션 단건 조회. */
    public InterviewSessionDto getOwnedSession(Long sessionId, Long memberId) {
        InterviewSessionDto session = interviewSessionMapper.findById(sessionId);
        if (session == null || !session.getMemberId().equals(memberId)) {
            return null;
        }
        return session;
    }

    public List<InterviewMessageDto> getMessages(Long sessionId) {
        return interviewMessageMapper.findBySessionId(sessionId);
    }

    /**
     * 지원자의 답변을 저장하고, AI의 첨삭 + 다음 질문을 받아 저장한 뒤 반환한다.
     * 호출부(Controller)에서 sessionId가 로그인한 memberId 소유인지 먼저 검증했다고 가정한다.
     */
    public InterviewMessageDto submitAnswer(InterviewSessionDto session, String answerContent) {
        if (answerContent == null || answerContent.isBlank()) {
            throw new InterviewIntegrationException("답변 내용을 입력해주세요.");
        }
        if ("ENDED".equals(session.getStatus())) {
            throw new InterviewIntegrationException("이미 종료된 면접입니다.");
        }

        InterviewMessageDto userMessage = new InterviewMessageDto();
        userMessage.setSessionId(session.getId());
        userMessage.setSender("USER");
        userMessage.setMessageType("ANSWER");
        userMessage.setContent(answerContent.trim());
        interviewMessageMapper.insert(userMessage);

        InterviewProjectDto project = interviewProjectMapper.findById(session.getProjectId());
        List<InterviewMessageDto> history = interviewMessageMapper.findBySessionId(session.getId());

        String aiReply = claudeAiService.continueInterview(toRepoInfo(project), history);
        InterviewMessageDto aiMessage = saveAiMessage(session.getId(), "FEEDBACK", aiReply);
        interviewSessionMapper.incrementQuestionCount(session.getId());

        return aiMessage;
    }

    public void endSession(Long sessionId) {
        interviewSessionMapper.endSession(sessionId);
    }

    // 홈 대시보드용 - 면접 준비율(%). 답변 제출 개수를 기준으로 계산하며 100을 넘지 않는다.
    public int getPrepRate(Long memberId) {
        int answeredCount = interviewMessageMapper.countAnsweredByMemberId(memberId);
        return Math.min(100, (int) Math.round(answeredCount * 100.0 / PREP_TARGET_ANSWERS));
    }

    private InterviewMessageDto saveAiMessage(Long sessionId, String type, String content) {
        InterviewMessageDto dto = new InterviewMessageDto();
        dto.setSessionId(sessionId);
        dto.setSender("AI");
        dto.setMessageType(type);
        dto.setContent(content);
        interviewMessageMapper.insert(dto);
        return dto;
    }

    private GitHubRepoInfo toRepoInfo(InterviewProjectDto project) {
        return new GitHubRepoInfo(
                project.getRepoOwner(),
                project.getRepoName(),
                project.getDescription(),
                project.getPrimaryLanguage(),
                project.getReadmeText()
        );
    }
}
