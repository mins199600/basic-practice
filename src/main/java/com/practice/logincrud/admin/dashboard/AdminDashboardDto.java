package com.practice.logincrud.admin.dashboard;

import com.practice.logincrud.board.BoardDto;
import com.practice.logincrud.member.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminDashboardDto {

    //상단 카드 영역
    private int totalBoard;     //전체 게시글 수
    private int totalMember;    //전체 회원 수
    private int totalNotice;    //공지사항
    private int activePopupCount;   //활성된 팝업 수

    //목록영역
    private List<UserDto> recentUsers;
    private List<BoardDto> recentBoards;

}
