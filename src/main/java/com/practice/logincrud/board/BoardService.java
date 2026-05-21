package com.practice.logincrud.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<BoardDto> getMyBoardList(Long memberId, PageDto pageDto) {
        return boardMapper.getMyBoardList(memberId, pageDto);
    }

    // 전체 게시글 수
    public int getMyBoardTotalCount(Long memberId) {
        return boardMapper.getMyBoardTotalCount(memberId);
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
}
