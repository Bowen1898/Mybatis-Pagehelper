package com.yuyuko.pagehelper;

import java.util.Map;

public class PageHelper {
    public Page<?> getPageFromParam(Object param) {
        PageParam pageParam = null;
        if (param instanceof PageParam) {
            pageParam = (PageParam) param;
        } else if (param instanceof Map) {
            for (Object obj : ((Map<?, ?>) param).values())
                if (obj instanceof PageParam) {
                    pageParam = (PageParam) obj;
                    break;
                }
        }

        if(pageParam == null)
            return null;

        return (Page<?>) new Page(pageParam.getPageNum(), pageParam.getPageSize(),pageParam.isCount());
    }
}
