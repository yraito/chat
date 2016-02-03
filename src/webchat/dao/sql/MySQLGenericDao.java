/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;
import webchat.dao.DaoException;
import webchat.dao.Searcher;
import webchat.dao.sql.query.MySQLSearcher;
import webchat.dao.sql.query.QueryParts;
import static webchat.util.StringUtils.*;

/**
 * Generic dao that uses mappings 
 * @author Nick
 */
public class MySQLGenericDao<T> {

    	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MySQLGenericDao.class);
	
    Mapping insertMapping;
    Mapping selectMapping;
    private Connection conn;
    private String findFieldName;
    Class<T> clazz;

    public MySQLGenericDao(Mapping insertMapping, Mapping selectMapping, Connection conn, String findFieldName, Class<T> clazz) {
        this.insertMapping = insertMapping;
        this.selectMapping = selectMapping;
        this.conn = conn;
        this.findFieldName = findFieldName;
        this.clazz = clazz;
    }

    
    public void save(T t) throws DaoException {

        String table = insertMapping.getEquivTable().toString();
        Map<String, Object> row = insertMapping.convertToRow(t);
        List<String> colList = new ArrayList<>(row.keySet());
        String cols = newString(colList);
        String vals = newString("?", row.size());
        String insertT = "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
        logger.debug("save Record {} : {} ", t, insertT);
        try (PreparedStatement pstmt = conn.prepareStatement(insertT)) {
            for (int colIndex = 0; colIndex < colList.size(); colIndex++) {
                int paramIndex = colIndex + 1;
                String colName = colList.get(colIndex);
                Object obj = row.get(colName);
                pstmt.setObject(paramIndex, obj);
            }
            int nrows = pstmt.executeUpdate();
            assert nrows == 1;
        } catch (SQLException e) {
            throw new DaoException("Exception saving " + t, e);
        }
    }

    public T find(String s) throws DaoException {

        String fromClause = selectMapping.getEquivTable().toString();
        List<String> colList = new ArrayList<>();
        for (Field selectedField : selectMapping.getSelectedFields()) {
            ColInfo colInfo = selectMapping.getEquivColumn(selectedField);
            colList.add(colInfo.toString());
        }

        String cols = newString(colList);
        String selectT = "SELECT " + cols + " FROM " + fromClause + " WHERE LOWER("
                + findFieldName + ") = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(selectT)) {
            pstmt.setObject(1, s.toLowerCase());
            logger.debug("find( {} ) : {} ; {}", s, selectT, s.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    logger.debug("found no {} for find({})", clazz.getSimpleName(), s);
                    return null;
                }
                TreeMap<String, Object> row = new TreeMap<>();
                for (String colName : colList) {
                    Object obj = rs.getObject(colName);
                    row.put(colName, obj);
                }
                assert !rs.next();
                T t = selectMapping.convertFromRow(row, clazz);
                logger.debug("found {} for find({{}): {}", clazz.getSimpleName(), s, t);
                return t;
            }

        } catch (SQLException e) {
            throw new DaoException("Exception finding " + s, e);
        }
    }

    public Searcher<T> searcher() {
        String cols = "*";
        String from = selectMapping.getEquivTable().toString();
        QueryParts qp = new QueryParts(cols, from);
        return new MySQLSearcher(selectMapping, conn, clazz, qp);
    }
}