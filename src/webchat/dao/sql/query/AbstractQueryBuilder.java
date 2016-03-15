/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql.query;

import java.util.List;
import webchat.dao.Where;
import webchat.dao.sql.ColInfo;
import webchat.dao.sql.TblInfo;

import static webchat.util.StringUtils.*;

/**
 *
 * @author Edward
 */
public abstract class AbstractQueryBuilder implements QueryBuilder {

    String select;
    String from;
    String where;
    String orderBy;
    String groupBy;
    String limit;

    @Override
    public QueryBuilder select(String c) {
        if (isNullOrEmpty(c)) {
            return this;
        }
        select = "SELECT " + c;
        return this;
    }

    @Override
    public QueryBuilder from(String t) {
        if (isNullOrEmpty(t)) {
            return this;
        }
        from = "FROM " + t;
        return this;
    }

    @Override
    public QueryBuilder where(String w) {
        if (isNullOrEmpty(w)) {
            return this;
        }
        where = "WHERE " + w;
        return this;
    }

    @Override
    public QueryBuilder groupBy(String c) {
        if (isNullOrEmpty(c)) {
            return this;
        }
        groupBy = "GROUP BY " + c;
        return this;
    }

    @Override
    public QueryBuilder sortBy(String c) {
        if (isNullOrEmpty(c)) {
            return this;
        }
        orderBy = "ORDER BY " + c;
        return this;
    }

    @Override
    public String build() {
        if (select == null || from == null) {
            throw new IllegalStateException("Missing select or from clause");
        }
        String s = select + " " + from;
        if (!isNullOrEmpty(where)) {
            s += " " + where;
        }
        if (!isNullOrEmpty(groupBy)) {
            s += " " + groupBy;
        }
        if (!isNullOrEmpty(orderBy)) {
            s += " " + orderBy;
        }
        if (!isNullOrEmpty(limit)) {
            s += " " + limit;
        }
        return s;
    }

    @Override
    public abstract QueryBuilder from(List<TblInfo> ts);

    @Override
    public abstract QueryBuilder limit(int num);

    @Override
    public abstract QueryBuilder limit(int start, int num);

}
