package com.yuyuko.pagehelper;

public class PageParam {

    protected int pageNum;

    protected int pageSize;

    protected boolean count;

    private int limitFrom;

    private int limitSize;

    public PageParam(int pageNum, int pageSize) {
        this(pageNum, pageSize, true);
    }

    public PageParam(int pageNum, int pageSize, boolean count) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;

        calculateLimit(pageNum, pageSize);
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isCount() {
        return count;
    }

    protected void calculateLimit(int pageNum,int pageSize){
        this.limitFrom = pageSize * (pageNum - 1);
        this.limitSize = pageSize;
    }

    public int getLimitFrom() {
        return limitFrom;
    }

    public int getLimitSize() {
        return limitSize;
    }
}