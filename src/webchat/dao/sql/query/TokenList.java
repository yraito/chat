package webchat.dao.sql.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TokenList {

	LinkedList<Token> tokens =new LinkedList<>();
	StringBuffer sb = new StringBuffer();
	List<Object> objs = new ArrayList<>();
	
	public TokenList(List<Token> tokens) {
		this.tokens.addAll(tokens);
	}
	
	public TokenList() {
		
	}
	
	void process() {
		sb = new StringBuffer();
		objs.clear();
		for (Token t : tokens) {
			t.exec(this);
		}
	}
	
	public String toString() {
		return sb.toString();
	}
}
