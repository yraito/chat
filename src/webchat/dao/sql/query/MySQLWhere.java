package webchat.dao.sql.query;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import webchat.dao.DaoException;
import webchat.dao.Where;
import webchat.dao.sql.Mapping;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MySQLWhere<T> implements Where<T> {

    private final static Logger logger = LoggerFactory.getLogger(MySQLWhere.class);

    TokenList tokenList;
    QueryParts queryParts;
    Connection conn;
    Class<T> clazz;
    Mapping mapping;
    //String str;

    public MySQLWhere(Mapping m, Connection conn, Class<T> clazz, QueryParts qp, List<Token> tokens) {
        this.mapping = m;
        this.conn = conn;
        this.clazz = clazz;
        this.queryParts = qp;
        this.tokenList = new TokenList(tokens);
    }

    public MySQLWhere(Mapping m, Connection conn, Class<T> clazz, QueryParts qp, Field f, String s, Object o) {
        this.mapping = m;
        this.conn = conn;
        this.clazz = clazz;
        this.queryParts = qp;
        tokenList = new TokenList();
        tokenList.tokens.add(new FieldToken(f, m));
        tokenList.tokens.add(new StringToken(s));
        tokenList.tokens.add(new ObjectToken(o));
    }

    MySQLWhere(Mapping m, Connection conn, Class<T> clazz, String fromPart) {
        this.mapping = m;
        this.conn = conn;
        this.clazz = clazz;
        this.queryParts = new QueryParts("*", fromPart);
    }

    MySQLWhere(Mapping m, Connection conn, String tableName, String s) {
        this.mapping = m;
        queryParts = new QueryParts("*", tableName);
        tokenList = new TokenList();
        tokenList.tokens.add(new StringToken(s));
    }

    private MySQLWhere(Mapping m, List<Token> ts) {
        this.mapping = m;
        queryParts = new QueryParts("*", "sometable");
        tokenList = new TokenList();
        tokenList.tokens.addAll(ts);
    }

    @Override
    public MySQLWhere<T> and(Where<T> that) {
        MySQLWhere<T> sqlthat = (MySQLWhere<T>) that;
        if (sqlthat.tokenList.tokens.isEmpty()) {
            return this;
        }
        if (this.tokenList.tokens.isEmpty()) {
            return sqlthat;
        }
        LinkedList<Token> l = new LinkedList<>();
        l.add(new StringToken("("));
        l.add(new PredicateToken(this));
        l.add(new StringToken(") AND ("));
        l.add(new PredicateToken(sqlthat));
        l.add(new StringToken(")"));
        return new MySQLWhere<>(mapping, conn, clazz, queryParts, l);
    }

    @Override
    public MySQLWhere<T> or(Where<T> that) {
        MySQLWhere<T> sqlthat = (MySQLWhere<T>) that;
        if (sqlthat.tokenList.tokens.isEmpty()) {
            return this;
        }
        if (this.tokenList.tokens.isEmpty()) {
            return sqlthat;
        }
        LinkedList<Token> l = new LinkedList<>();
        l.add(new StringToken("("));
        l.add(new PredicateToken(this));
        l.add(new StringToken(") OR ("));
        l.add(new PredicateToken(sqlthat));
        l.add(new StringToken(")"));
        return new MySQLWhere<>(mapping, conn, clazz, queryParts, l);
    }

    @Override
    public List<T> doQuery() throws DaoException {
        return doQuery(null, null, null);
    }

    @Override
    public List<T> doQuery(Field sortBy, Integer start, Integer num) throws DaoException {
        tokenList.process();
        if (queryParts.wherePart != null) {
            queryParts.wherePart = "(" + queryParts.wherePart + ")";
            queryParts.wherePart += " AND ";
            queryParts.wherePart += tokenList.sb.toString();
        } else {
            queryParts.wherePart = tokenList.sb.toString();
        }
        if (sortBy != null) {
            queryParts.orderByPart = mapping.getEquivColumn(sortBy).toString();
        }
        queryParts.limitStart = start;
        queryParts.limitNum = num;
        String query = queryParts.buildQuery();
        List<T> results = runQuery(query, tokenList.objs);
        return results;
    }

    public String toString() {
        if (queryParts.wherePart == null) {
            tokenList.process();
            queryParts.wherePart = tokenList.sb.toString();
        }
        return queryParts.wherePart;
    }

    private List<T> runQuery(String prepQuery, List<Object> queryArgs) throws DaoException {

        logger.debug("runQuery: {} ; {}", prepQuery, queryArgs);
        try (PreparedStatement pstmt = conn.prepareStatement(prepQuery)) {

            for (int j = 0; j < queryArgs.size(); j++) {
                pstmt.setObject(j + 1, queryArgs.get(j));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> rsltList = new ArrayList<>();
                while (rs.next()) {

                    T t = clazz.newInstance();
                    for (Field f : mapping.getSelectedFields()) {
                        String colName = mapping.getEquivColumn(f).toString();
                        //hack
                          colName = colName.replace(".", "__");
                        //
                        Object o = rs.getObject(colName);
                        f.set(t, o);
                    }
                    rsltList.add(t);
                }
                logger.debug("{} results", rsltList.size());
                return rsltList;
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Can't instantiate/set new " + clazz.getName(), e);
        }
    }
    

    public static void main(String[] args) {
        LinkedList<Token> l = new LinkedList<>();
        l.add(new StringToken("YADA"));
        l.add(new StringToken(">"));
        l.add(new StringToken("BLAH"));
        MySQLWhere s = new MySQLWhere(null, l);
        LinkedList<Token> l2 = new LinkedList<>();
        l2.add(new StringToken("L"));
        l2.add(new StringToken("IN"));
        Date d = new Date();
        Integer i = -1000;
        l2.add(new ObjectToken(d));
        MySQLWhere s2 = new MySQLWhere(null, l2);

        LinkedList<Token> l3 = new LinkedList<>();
        l3.add(new StringToken("YADA"));
        l3.add(new StringToken(">"));
        l3.add(new ObjectToken(-1000));
        s2 = s2.and(new MySQLWhere(null, l3)).or(s);
        System.out.println(s2);
        System.out.println(s2.tokenList.objs);

    }

}
