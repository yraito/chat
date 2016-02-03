/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

/**
 *
 * @author Nick
 */
public class ColInfo {

    public ColInfo(String name, String realName, String tableAlias) {
        this.realName = realName;
        this.realTable = tableAlias;
        this.name = name;
    }

    public ColInfo(String name) {
        this.name = name;
    }
    
    String realName;
    String realTable;
    String name;

    public String getRealName() {
        return realName;
    }

    public String getRealTableNameOrAlias() {
        return realTable;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (realName == null) {
            return name;
        } else {
            return realTable + "." + realName + " AS " + name;
        }
    }
}
