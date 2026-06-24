package com.practice.logincrud.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    //전체조회
    public List<BoardDto> findAll() {
        return boardMapper.findAll();
    }

    // 페이징 목록 조회
    public List<BoardDto> getMyBoardList(PageDto pageDto) {
        return boardMapper.getMyBoardList(pageDto);
    }

    // 전체 게시글 수(필터 없음)
    public int getMyBoardTotalCount() {
        return boardMapper.getMyBoardTotalCount();
    }

    //전체 게시글 수(필터 적용)
    public int getMyBoardTotalCountByFilter(PageDto pageDto) {
        return boardMapper.getMyBoardTotalCountByFilter(pageDto);
    }

    //상세조회
    public BoardDto findById(Long id) {
        return boardMapper.findById(id);
    }

    //글쓰기
    public void save(BoardDto boardDto) {
        boardMapper.save(boardDto);
    }

    //게시글 수정
    public void update(BoardDto boardDto) {
        boardMapper.update(boardDto);
    }

    //게시글 삭제
    public void delete(Long id) {
        boardMapper.delete(id);
    }

    //검색어 처리
    public List<BoardDto> getBoardList(Map<String, Object> params) {
        return boardMapper.getBoardList(params);
    }

    // 검색 결과 총 개수
    public int getBoardSearchCount(Map<String, Object> params) {
        return boardMapper.getBoardSearchCount(params);
    }

    // 조회수 증가
    public void increaseViewCount(Long id) {
        boardMapper.increaseViewCount(id);
    }


}
