/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.util;

import java.lang.reflect.Field;
import java.util.function.Function;
import webchat.dao.Searcher;
import webchat.dao.Where;

/**
 *
 * @author Nick
 */
public class FromParamsSearcher<T> {

    Searcher<T> s;
    Class<T> clazz;

    public FromParamsSearcher(Searcher<T> s, Class<T> clazz) {
        this.s = s;
        this.clazz = clazz;
    }

    public Where<T> whereEqAny(String field, Object... os) {
        return whereAny(o -> s.whereEq(getField(field), o), os);
    }

    public Where<T> whereLwrEqAny(String field, String... os) {
        return whereAny(o -> s.whereLwrEq(getField(field), (String) o), (Object[]) os);
    }

    public Where<T> whereContainsAny(String field, String... os) {
        return whereAny( o -> s.whereLike(getField(field), "%" + (String) o + "%"), (Object[]) os);
    }

    public Where<T> whereContainsAll(String field, String... os) {
        return whereAll( o -> s.whereLike(getField(field), "%" + (String) o + "%"), (Object[]) os);
    }

    public Where<T> whereContains(String field, boolean all, String... os) {
        if (all) {
            return whereContainsAll(field, os);
        } else {
            return whereContainsAny(field, os);
        }
    }

    public Where<T> whereNeqAll(String field, Object... os) {
        return whereAll( o -> s.whereNeq(getField(field), o), os);
    }

    public Where<T> whereBtw(String field, Object a, Object b) {
        Field f = getField(field);
        if (a == null && b != null) {
            return s.whereLte(f, b);
        } else if (a != null && b == null) {
            return s.whereGte(f, a);
        } else if (a == null && b == null) {
            return s.where1is1();
        } else {
            return s.whereBtw(getField(field), a, b);
        }
    }

    public Where<T> whereNotNull(String field) {
        return s.whereNotNull(getField(field));
    }

    public Where<T> whereNull(String field) {
        return s.whereNull(getField(field));
    }

    public Where<T> whereAny(Function<Object, Where<T>> func, Object... keywords) {
        if (keywords == null || keywords.length == 0) {
            return s.where1is1();
        }

        Where<T> whereTotal = func.apply(keywords[0]);
        for (int j = 1; j < keywords.length; j++) {
            Where<T> where = func.apply(keywords[j]);
            whereTotal = whereTotal.or(where);
        }
        return whereTotal;
    }

    public Where<T> whereAll(Function<Object, Where<T>> func, Object... keywords) {
        if (keywords == null || keywords.length == 0) {
            return s.where1is1();
        }

        Where<T> whereTotal = func.apply(keywords[0]);
        for (int j = 1; j < keywords.length; j++) {
            Where<T> where = func.apply(keywords[j]);
            whereTotal = whereTotal.and(where);
        }
        return whereTotal;
    }

    private Field getField(String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field: " + clazz.getName() + "::" + field);
        }
    }
}
