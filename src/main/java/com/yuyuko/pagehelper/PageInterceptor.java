package com.yuyuko.pagehelper;

import com.yuyuko.pagehelper.utils.ExecutorUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PageInterceptor implements Interceptor {
    private final PageHelper pageHelper = new PageHelper();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object param = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;
        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = ms.getBoundSql(param);
            cacheKey = executor.createCacheKey(ms, param, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        List<?> res = null;

        Page<?> page = pageHelper.getPageFromParam(param);
        if (page != null) {//need paging
            if (page.isCount()) {
                long count = count(executor, ms, param, resultHandler, boundSql);
                afterCount(page, count);
                if (count == 0)
                    res = new ArrayList<>();
            }
            if (res == null) {
                res = executor.query(ms, param, rowBounds, resultHandler, cacheKey, boundSql);
            }
        } else
            return executor.query(ms, param, rowBounds, resultHandler, cacheKey, boundSql);
        return paging(page, res);
    }

    @SuppressWarnings("unchecked")
    private Object paging(Page page, List res) {
        page.addAll(res);
        return page;
    }

    private long count(Executor executor, MappedStatement ms, Object param,
                       ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String countMsId = getCountMsId(ms);
        long count;
        MappedStatement countMs = ExecutorUtils.getExistedMappedStatement(ms.getConfiguration(), countMsId);

        if (countMs == null) {
            countMs = ExecutorUtils.createCountMappedStatement(ms, countMsId, param, boundSql);
            ms.getConfiguration().addMappedStatement(countMs);
        }

        count = ExecutorUtils.executeCountSql(executor, countMs, param, boundSql, resultHandler);
        return count;
    }

    private void afterCount(Page<?> page, long count) {
        page.setTotal(count);
    }

    private String getCountMsId(MappedStatement ms) {
        String methodFullName = ms.getId();
        int lastDotPos = methodFullName.lastIndexOf('.');
        String methodName = methodFullName.substring(lastDotPos + 1);
        String countMethodName = String.format("count%c%s", Character.toUpperCase(methodName.charAt(0)), methodName.substring(1));
        return methodFullName.substring(0, lastDotPos + 1).concat(countMethodName);
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
