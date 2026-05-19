package com.practice.logincrud.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageDto {

    private int page = 1;
    private int pageSize = 10;

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getStartNo() {
        return (page - 1) * pageSize + 1;
    }
}
