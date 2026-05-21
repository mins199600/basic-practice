package com.practice.logincrud.board;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    //전체조회
    List<BoardDto> findAll();

    //상세조회
    BoardDto findById(Long id);

    //글쓰기
    void save(BoardDto boardDto);

    //게시글 수정
    void update(BoardDto boardDto);

    //게시글 삭제
    void delete(Long id);

    // 페이징 추가
    List<BoardDto> getMyBoardList(@Param("memberId") Long memberId, PageDto pageDto);
    int getMyBoardTotalCount(@Param("memberId") Long memberId);



}
