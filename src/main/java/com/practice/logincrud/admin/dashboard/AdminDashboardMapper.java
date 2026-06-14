package com.practice.logincrud.admin.dashboard;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDashboardMapper {

    //전체 공지사항 수
    int getNoticeBoardCount();

}
