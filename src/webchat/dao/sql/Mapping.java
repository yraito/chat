/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * A mapping from a class to a table
 * 
 * @author Nick
 * TODO Exception handling...
 */
public class Mapping {
    
    Class<?> clazz;
    TblInfo equivTable;
    Map<Field, ColInfo> map;
    
    Mapping(Class<?> clazz, TblInfo equivTable, Map<Field, ColInfo> map) {
        this.clazz = clazz;
        this.equivTable = equivTable;
        this.map = map;
        for (Field field : map.keySet()) {
            field.setAccessible(true);
        }
    }
    
    public Class<?> getClazz() {
        return clazz;
    }
    
    public TblInfo getEquivTable() {
        return equivTable;
    }
    
    public Collection<Field> getSelectedFields() {
        return map.keySet();
    }
    
    public ColInfo getEquivColumn(Field f) {
        return map.get(f);
    }

    public Map<String, Object> convertToRow(Object obj) {
        try {
            TreeMap<String, Object> row = new TreeMap<>();
            for (Field field : getSelectedFields()) {
                String colName = getEquivColumn(field).toString();
                Object colValue = field.get(obj);
                row.put(colName, colValue);
            }
            return row;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    } 
    
    public <T> T convertFromRow(Map<String, Object> row, Class<T> clazz) {
        try {
            T obj = clazz.newInstance();
            for (Field field : getSelectedFields()) {
                String colName = getEquivColumn(field).toString();
                Object colValue = row.get(colName); //case sensitive
                field.set(obj, colValue);
            }
            return obj;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        } 

    }
}
