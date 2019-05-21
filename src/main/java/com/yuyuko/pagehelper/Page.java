package com.yuyuko.pagehelper;

import java.util.ArrayList;

public class Page<E> extends ArrayList<E> {
    private int pageNum;

    private int pageSize;

    private long total;

    private int pages;

    private boolean count;

    public Page(int pageNum, int pageSize, boolean count) {
        super(0);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
    }

    public PageInfo<E> toPageInfo(){
        return new PageInfo<>(this);
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public void setTotal(long total) {
        this.total = total;
        if (pageSize > 0)
            pages = (int) (this.total / pageSize + ((this.total % pageSize == 0) ? 0 : 1));
        else
            pages = 0;
    }

    public long getTotal() {
        return total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}