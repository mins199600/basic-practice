package com.practice.logincrud.board;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BoardMapper {

    //전체 게시글 수
    int getMyBoardTotalCount();

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
    List<BoardDto> getMyBoardList(@Param("pageDto") PageDto pageDto);

    //검색어
    List<BoardDto> getBoardList(Map<String, Object> params);

    // 검색 결과 총 개수
    int getBoardSearchCount(Map<String, Object> params);

    int getMyBoardTotalCount(PageDto pageDto);


}
