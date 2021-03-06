/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import webchat.dao.dto.Column;
import webchat.dao.dto.Reference;
import webchat.dao.dto.Table;
import static webchat.util.StringUtils.*;
/**
 * Generates Mappings from annotated classes to database tables. <br>
 *
 * More precisely, a @Table annotated class is mapped to a 'FROM' clause, and
 * the fields of the class mapped to the columns. We define two mappings for
 * each class. The 'insert' mapping is used for inserts and updates, and maps a
 * class to its corresponding table, omitting @Reference annotated fields. The
 * 'select' mapping is used for retrievals, and includes @Reference fields by
 * left joining the corresponding table on its foreign keys. This allows us to
 * 'expand' the details for the foreign keys.
 *
 * @author Nick
 */
public class Mapper {

    public Mapping getMapping(Class<?> clazz, List<Field> fields) {

        checkAnnoPresent(clazz, Table.class);
        String mainTbl = clazz.getAnnotation(Table.class).name();
        Field pkField = null;
        HashMap<Field, ColInfo> colInfos = new HashMap<>();
        TreeMap<String, String[]> joinInfos = new TreeMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Reference.class)) {
                ColInfo colInfo = getColInfo(clazz, field);
                colInfos.put(field, colInfo);
                String[] joinInfo = getJoinInfo(clazz, field);
                if (!joinInfos.containsKey(joinInfo[0])) {
                    joinInfos.put(joinInfo[0], joinInfo);
                }
            } else {
                if (field.getAnnotation(Column.class).isPrimaryKey()) {
                    pkField = field;
                }
                String fieldAlias = field.getAnnotation(Column.class).name();
                ColInfo colInfo = new ColInfo(fieldAlias, fieldAlias, mainTbl);
                colInfos.put(field, colInfo);
            }
        }
        TblInfo tblInfo = new TblInfo(mainTbl);
        for (String[] joinInfo : joinInfos.values()) {
            tblInfo.addJoinedTable(joinInfo[0], joinInfo[1], joinInfo[2], joinInfo[3]);
        }
        return new Mapping(clazz, tblInfo, pkField, colInfos);
    }

    private ColInfo getColInfo(Class<?> clazz, Field refAnnoField) {
        try {
            Reference reference = refAnnoField.getAnnotation(Reference.class);
            Field foreignKeyField = clazz.getDeclaredField(reference.foreignKeyField());
            checkAnnoPresent(refAnnoField, Column.class);
            checkAnnoPresent(foreignKeyField, Column.class);
            String fieldAlias = refAnnoField.getAnnotation(Column.class).name();
            String fieldRealOwnerClass = foreignKeyField.getAnnotation(Column.class).isForeignKeyOf();
            checkMapping(!isNullOrEmpty(fieldRealOwnerClass), "Missing isForeignKeyOf attribute on @Column " 
                    + clazz.getName() + ":: " + foreignKeyField.getName());
            Class fieldRealOwnerClassClass = Class.forName(fieldRealOwnerClass);
            String foreignKeyName = foreignKeyField.getAnnotation(Column.class).name();
            String fieldRealOwnerAlias = getTableAlias(fieldRealOwnerClass, foreignKeyName);
            System.out.println(clazz.getName() + ":" + reference.foreignField());
            Field fieldRealField = fieldRealOwnerClassClass.getDeclaredField(reference.foreignField());
            String fieldRealName = fieldRealField.getAnnotation(Column.class).name();
            return new ColInfo(fieldAlias, fieldRealName, fieldRealOwnerAlias);
        } catch (NoSuchFieldException e) {
            throw new MappingException("Error resolving reference anno on field "
                    + refAnnoField + ". No such field", e);
        } catch (ClassNotFoundException e) {
            throw new MappingException("Error resolving foreign key owner class, no such class", e);
        }

    }

    private String[] getJoinInfo(Class<?> clazz, Field refAnnoField) {

        Reference reference = refAnnoField.getAnnotation(Reference.class);
        try {
            Field foreignKeyField = clazz.getDeclaredField(reference.foreignKeyField());
            checkAnnoPresent(refAnnoField, Column.class);
            checkAnnoPresent(foreignKeyField, Column.class);
            String foreignKeyName = foreignKeyField.getAnnotation(Column.class).name();
            String fieldRealOwnerClass = foreignKeyField.getAnnotation(Column.class).isForeignKeyOf();
            String primaryKeyName = getPrimaryKeyColumn(fieldRealOwnerClass);
            String tableAlias = getTableAlias(fieldRealOwnerClass, foreignKeyName);
            Class<?> fieldRealOwnerClassClass = Class.forName(fieldRealOwnerClass);
            String tableRealName = fieldRealOwnerClassClass.getAnnotation(Table.class).name();
            return new String[]{tableAlias, tableRealName, foreignKeyName, primaryKeyName};
        } catch (NoSuchFieldException e) {
            throw new MappingException("Error resolving reference anno on field "
                    + refAnnoField + ". No such field", e);
        } catch (ClassNotFoundException e) {
            throw new MappingException("Error on anno for " + reference.foreignKeyField() + " isForeignKeyOf."
                    + " class not found");
        }
    }

    private String getPrimaryKeyColumn(String className) {

        try {
            Class<?> clazz = Class.forName(className);
            checkAnnoPresent(clazz, Table.class);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    if (field.getAnnotation(Column.class).isPrimaryKey()) {
                        return field.getAnnotation(Column.class).name();
                    }
                }
            }
            throw new MappingException(className + " missing primary key anno");
        } catch (ClassNotFoundException e) {
            throw new MappingException("class " + className + " not found"); //
        }
    }

    private String getTableAlias(String joinedTableClass, String foreignKeyName) {
        return foreignKeyName + "_Join";
    }

    public Mapping getMappingForInsert(Class<?> clazz) {
        ArrayList<Field> annoFields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                if (!field.isAnnotationPresent(Reference.class)) {
                    annoFields.add(field);
                }
            }
        }
        return getMapping(clazz, annoFields);
    }

    public Mapping getMappingForSelect(Class<?> clazz) {
        ArrayList<Field> annoFields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                annoFields.add(field);
            }
        }
        return getMapping(clazz, annoFields);
    }

    public void checkAnnoPresent(AnnotatedElement ae, Class annoClass) {
        boolean b = ae.isAnnotationPresent(annoClass);
        checkMapping(b, "Missing anno " + annoClass + " on " + ae);
    }

    public void checkMapping(boolean b, String err) {
        if (!b) {
            throw new MappingException(err);
        }
    }
}
