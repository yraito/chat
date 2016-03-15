package webchat.dao.sql.query;

import webchat.dao.sql.MySQLDaoConnectionFactory;
import static webchat.util.StringUtils.*;

public class QueryParts {

	public String selectPart;
	public String fromPart;
	public String wherePart;
	public String groupByPart;
	public String orderByPart;
	public Integer limitStart;
	public Integer limitNum;
	
	public QueryParts(String selectPart, String fromPart) {
		super();
		this.selectPart = selectPart;
		this.fromPart = fromPart;
	}
	
	public String buildQuery() {
		/*String s = "SELECT " + selectPart +
				" FROM " + fromPart;
		if ( !isNullOrEmpty(wherePart) ) {
			s += " WHERE " + wherePart;
		}
		if ( !isNullOrEmpty(groupByPart) ) {
			s += " GROUP BY " + groupByPart;
		} 
		if ( !isNullOrEmpty(orderByPart) ) {
			s += " ORDER BY " + orderByPart;
		}
		if ( limitNum != null) {
			if (limitStart != null) {
				s += " LIMIT " + limitStart + "," + limitNum;
			} else {
				s += " LIMIT " + limitNum;
			}
		}
		return s;*/
                QueryBuilder qb = MySQLDaoConnectionFactory.getInstance().getQueryBuilder();
                qb = qb.select(selectPart).from(fromPart).where(wherePart).groupBy(groupByPart).sortBy(orderByPart);
                if (limitNum != null) {
                    if (limitStart != null) {
                        qb = qb.limit(limitStart, limitNum);
                    } else {
                        qb = qb.limit(limitNum);
                    }
                }
                return qb.build();
	}
}
