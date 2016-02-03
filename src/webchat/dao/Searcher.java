package webchat.dao;

import java.lang.reflect.Field;

/**
 * An abstraction for creating a simple class of query in which the where clause just
 * restricts fields to specified ranges. E.g. select * from products where Price < 100 and
 * Department = Jewelry. This is basically all the functionality needed for a web-based 
 * filtered search.
 * 
 * The Searcher is used to create an atomic predicate (a Where object) such as "Product.price < 100",
 * which can then be composed with other predicates using the and() and or() methods of the 
 * Where interface.
 * 
 * The attribute to restrict is passed as a Field of an object, not as a column name.
 * 
 * @author Nick
 *
 * @param <T>
 */
public interface Searcher<T> {

	/**
	 * An empty where clause, or equivalently, "where 1=1"
	 * @return
	 */
	Where<T> where1is1();
	
	Where<T> whereEq(Field f, Object obj);
	
	Where<T> whereNeq(Field f, Object obj);
	
	Where<T> whereLt(Field f, Object obj);
	
	Where<T> whereGt(Field f, Object obj);
	
	Where<T> whereLte(Field f, Object obj);
	
	Where<T> whereGte(Field f, Object obj);
	
	Where<T> whereIn(Field f, Object[] obj);
	
	Where<T> whereBtw(Field f, Object a, Object b);	
	
	Where<T> whereLike(Field f, String pat);
	
	Where<T> whereLwrEq(Field f, String s);
	
	Where<T> whereNull(Field f);
	
	Where<T> whereNotNull(Field f);
	
}
