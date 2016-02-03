package webchat.dao.sql.query;

import java.util.List;

public class PredicateToken implements Token {

	MySQLWhere w;
	
	@Override
	public void exec(TokenList tl) {
		List<Token> tokens = w.tokenList.tokens;
		for (Token t : tokens) {
			t.exec(tl);
		}
	}

	PredicateToken(MySQLWhere w) {
		this.w = w;
	}

}
