package webchat.dao.sql.query;

public class StringToken implements Token{

	String s;
	
	@Override
	public void exec(TokenList tl) {
		tl.sb.append(s);
		tl.sb.append(" ");
	}

	public StringToken(String s) {
		super();
		this.s = s;
	}
}
