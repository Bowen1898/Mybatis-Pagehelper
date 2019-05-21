package com.yuyuko.pagehelper.utils;


import com.yuyuko.pagehelper.PageException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 从延迟关联查询中生成count查询
 */
public class CountSqlParser {

    /**
     * 默认延迟关联的主查询是from中第一个子查询，将此子查询改写为count查询
     *
     * @param sql
     * @return
     */
    public static String getCountSql(String sql) {
        SelectBody deferredJoinSelectBody = getFirstSubSelectBody(sql);
        return generateCountSql(deferredJoinSelectBody);
    }

    private static String generateCountSql(SelectBody deferredJoinSelectBody) {
        if(deferredJoinSelectBody instanceof PlainSelect){
            PlainSelect plainSelect = (PlainSelect)deferredJoinSelectBody;
            List<SelectItem> countSelectItem = new ArrayList<>(1);
            countSelectItem.add(new SelectExpressionItem(new Column("count(0)")));
            plainSelect.setSelectItems(countSelectItem);
            return plainSelect.toString();
        }
        else
            throw new PageException("延迟关联子查询不是普通查询（带有with子句或union子句）");
    }


    private static SelectBody getFirstSubSelectBody(String sql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new PageException("解析SQL错误！");
        }

        Select select = (Select) statement;
        SelectBody selectBody = select.getSelectBody();
        return getFirstSubSelectBody(selectBody);
    }

    private static SelectBody getFirstSubSelectBody(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            return getFirstSubSelectBody((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSelectBody() != null)
                return getFirstSubSelectBody(withItem.getSelectBody());
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getSelects() != null && operationList.getSelects().size() > 0)
                return getFirstSubSelectBody(operationList.getSelects().get(0));
        }

        throw new PageException("没有发现延迟关联的子查询！");
    }

    private static SelectBody getFirstSubSelectBody(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        if (!(fromItem instanceof SubSelect))
            throw new PageException("延迟关联子查询没有紧跟在from后！");

        return ((SubSelect) fromItem).getSelectBody();
    }
}
