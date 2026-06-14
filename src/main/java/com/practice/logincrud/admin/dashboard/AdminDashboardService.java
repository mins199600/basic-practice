package com.practice.logincrud.admin.dashboard;

import com.practice.logincrud.admin.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    //전체 게시글 수
    public int getNoticeBoardCount() {
        return adminDashboardMapper.getNoticeBoardCount();
    }

    //관리자 팝업
    public int getActivePopupCount() {
        return adminDashboardMapper.getActivePopupCount();
    }

}
