package com.practice.logincrud.board;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {
    //전체조회

    //상세조회

    //글쓰기
    void save(BoardDto boardDto);

    //게시글 수정

    //게시글 삭제
}
