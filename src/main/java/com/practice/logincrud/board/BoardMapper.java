package com.practice.logincrud.board;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {
    //전체조회
    List<BoardDto> findAll();

    //상세조회
    BoardDto findById(Integer id);

    //글쓰기
    void save(BoardDto boardDto);

    //게시글 수정
    void update(BoardDto boardDto);

    //게시글 삭제
    void delete(Integer id);

    // 페이징 추가
    List<BoardDto> getBoardList(PageDto pageDto);
    int getBoardTotalCount();


}
