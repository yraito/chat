/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nick
 */
public class TblInfo {
    
    String mainTbl;
    List<String[]> joinedTbls = new ArrayList<>();

    public TblInfo(String mainTbl) {
        this.mainTbl = mainTbl;
    }
    
    public void addJoinedTable(String alias, String realName, String joinOnFkCol, String joinOnPkCol) {
        joinedTbls.add(new String[]{alias, realName, joinOnFkCol, joinOnPkCol});
    }

    public String getMainTable() {
        return mainTbl;
    }
    
    public int getNumberOfJoins() {
        return joinedTbls.size();
    }

    public String getJoinedTableAlias(int index) {
        return joinedTbls.get(index)[0];
    }

    public String getJoinedTableRealName(int index) {
        return joinedTbls.get(index)[1];
    }

    public String getJoinedOnFkCol(int index) {
        return joinedTbls.get(index)[2];
    }
    
    public String getJoinedOnPkCol(int index) {
        return joinedTbls.get(index)[3];
    }

    @Override
    public String toString() {
        String s = getMainTable();
        for (int index = 0; index < getNumberOfJoins(); index++) {
            s += " LEFT JOIN " + getJoinedTableRealName(index) 
                    + " AS " + getJoinedTableAlias(index) 
                    + " ON " + getJoinedOnFkCol(index)
                    + " = " + getJoinedTableAlias(index) + "." + getJoinedOnPkCol(index);
        }
        return s;
    }
    
    
}
