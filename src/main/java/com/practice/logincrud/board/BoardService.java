package com.practice.logincrud.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    //전체조회

    //상세조회

    //글쓰기
    public void save(BoardDto boardDto) {
        boardMapper.save(boardDto);
    }


    //게시글 수정

    //게시글 삭제
}
