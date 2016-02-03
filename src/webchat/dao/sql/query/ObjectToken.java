package webchat.dao.sql.query;

public class ObjectToken implements Token{

	Object obj;
	
	@Override
	public void exec(TokenList tl) {
		tl.objs.add(obj);
		tl.sb.append("?");
		tl.sb.append(" ");
	}

	public ObjectToken(Object obj) {
		super();
		this.obj = obj;
	}

}
