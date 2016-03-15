/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql.query;

import java.util.List;
import webchat.dao.sql.TblInfo;
import webchat.util.StringUtils;

/**
 *
 * @author Edward
 */
public class MySQLQueryBuilder extends AbstractQueryBuilder{

    @Override
    public QueryBuilder from(List<TblInfo> ts) {
        from = StringUtils.newString(ts, " LEFT JOIN ");
        return this;
    }

    @Override
    public QueryBuilder limit(int num) {
        limit = "LIMIT " + num;
        return this;
    }
    
    @Override
    public QueryBuilder limit(int start, int num) {
        limit = "LIMIT " + start + ", " + num;
        return this;
    }
    
    
}
