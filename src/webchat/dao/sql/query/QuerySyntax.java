/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql.query;

import java.util.List;
import webchat.dao.sql.TblInfo;
/**
 *
 * @author Edward
 */
public interface QuerySyntax {
    
    String leftJoin();
    
    String limit(int start, int num);
}
