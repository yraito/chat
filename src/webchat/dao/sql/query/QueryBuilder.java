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

/**
 *
 * @author Edward
 */
public interface QueryBuilder {
    
    QueryBuilder select(String c);
   
    QueryBuilder from(String t);

    QueryBuilder from(List<TblInfo> ts);
    
    QueryBuilder where(String w);
    
    QueryBuilder groupBy(String c);
    
    QueryBuilder sortBy(String c);
    
    QueryBuilder limit(int n);
    
    QueryBuilder limit(int x, int n);
    
    String build();
}
