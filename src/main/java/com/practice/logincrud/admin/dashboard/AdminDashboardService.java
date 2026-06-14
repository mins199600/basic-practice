package com.practice.logincrud.admin.dashboard;

import com.practice.logincrud.admin.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public int getNoticeBoardCount() {
        return adminDashboardMapper.getNoticeBoardCount();
    }
}
