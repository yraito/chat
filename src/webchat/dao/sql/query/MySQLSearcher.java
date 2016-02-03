package webchat.dao.sql.query;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import webchat.dao.Searcher;
import webchat.dao.Where;
import webchat.dao.sql.Mapping;

public class MySQLSearcher<T> implements Searcher<T>{

        Mapping mapping;
	Connection conn;
	Class<T> clazz;
	//String fromPart;
	QueryParts qp;
	

	public MySQLSearcher(Mapping mapping, Connection conn, Class<T> clazz, String fromPart) {
		super();
                this.mapping = mapping;
		this.conn = conn;
		this.clazz = clazz;
		this.qp = new QueryParts("*", fromPart);
	}
	

	public MySQLSearcher(Mapping mapping, Connection conn, Class<T> clazz, QueryParts qp) {
		super();
                this.mapping = mapping;
		this.conn = conn;
		this.clazz = clazz;
		this.qp = qp;
	}
	

	@Override
	public MySQLWhere<T> where1is1() {
		List<Token> ts = Collections.emptyList();
		return new MySQLWhere<>(mapping, conn, clazz, qp, ts);
	}

	@Override
	public MySQLWhere<T> whereEq(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, "=", obj);
	}

	@Override
	public MySQLWhere<T> whereNeq(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, "<>", obj);
	}

	@Override
	public MySQLWhere<T> whereLt(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, "<", obj);
	}

	@Override
	public MySQLWhere<T> whereGt(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, ">", obj);
	}

	@Override
	public MySQLWhere<T> whereLte(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, "<=", obj);
	}

	@Override
	public MySQLWhere<T> whereGte(Field f, Object obj) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, ">=", obj);
	}

	@Override
	public MySQLWhere<T> whereIn(Field f, Object[] obj) {
		ArrayList<Token> l = new ArrayList<>();
		l.add(new FieldToken(f, mapping));
		l.add(new StringToken("IN"));
		l.add(new StringToken("("));
		for (int j = 0; j < obj.length; j++) {
			l.add(new ObjectToken(obj[j]));
			if (j < obj.length - 1) {
				l.add(new StringToken(","));
			}
		}
		l.add(new StringToken(")"));
		return new MySQLWhere<>(mapping, conn, clazz, qp, l);
	}

	@Override
	public MySQLWhere<T> whereBtw(Field f, Object a, Object b) {
		ArrayList<Token> l = new ArrayList<>();
		l.add(new FieldToken(f, mapping));
		l.add(new StringToken("BETWEEN"));
		l.add(new ObjectToken(a));
		l.add(new StringToken("AND"));
		l.add(new ObjectToken(b));
		return new MySQLWhere<>(mapping, conn, clazz, qp, l);
	}


	@Override
	public Where<T> whereLike(Field f, String pat) {
		return new MySQLWhere<>(mapping, conn, clazz, qp, f, "LIKE", pat);
	}


	@Override
	public Where<T> whereLwrEq(Field f, String s) {
		ArrayList<Token> l = new ArrayList<>();
		l.add(new StringToken("LOWER("));
		l.add(new FieldToken(f, mapping));
		l.add(new StringToken(")="));
		l.add(new ObjectToken(s.toLowerCase()));
		return new MySQLWhere<>(mapping, conn, clazz, qp, l);
	}


	@Override
	public Where<T> whereNull(Field f) {
		ArrayList<Token> l = new ArrayList<>();
		l.add(new FieldToken(f, mapping));
		l.add(new StringToken("IS NULL"));
		return new MySQLWhere<>(mapping, conn, clazz, qp, l);
	}


	@Override
	public Where<T> whereNotNull(Field f) {
		ArrayList<Token> l = new ArrayList<>();
		l.add(new FieldToken(f, mapping));
		l.add(new StringToken("IS NOT NULL"));
		return new MySQLWhere<>(mapping, conn, clazz, qp, l);
	}

	
}
