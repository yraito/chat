package webchat.util;

import java.util.function.Function;

import webchat.dao.Searcher;
import webchat.dao.Where;

public class DaoUtils {

	public static <T> Where<T> whereAny(Searcher<T> searcher, Function<String, Where<T>> func, String...keywords) {
		if ( keywords == null || keywords.length == 0 ) {
			return searcher.where1is1();
		}
		
		Where<T> whereTotal = func.apply(keywords[0]);
		for (int j = 1; j < keywords.length; j++) {
			Where<T> where = func.apply(keywords[j]);
			whereTotal = whereTotal.or(where);
		}
		return whereTotal;
	}
	
	public static <T> Where<T> whereAll(Searcher<T> searcher, Function<String, Where<T>> func, String...keywords) {
		if ( keywords == null || keywords.length == 0 ) {
			return searcher.where1is1();
		}
		
		Where<T> whereTotal = func.apply(keywords[0]);
		for (int j = 1; j < keywords.length; j++) {
			Where<T> where = func.apply(keywords[j]);
			whereTotal = whereTotal.and(where);
		}
		return whereTotal;
	}
}
