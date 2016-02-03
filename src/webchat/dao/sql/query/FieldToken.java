package webchat.dao.sql.query;

import java.lang.reflect.Field;
import webchat.dao.sql.Mapping;

public class FieldToken implements Token {

	Field f;
	Mapping m;
        
	@Override
	public void exec(TokenList tl) {
		String fieldName = m.getEquivColumn(f).toString();
		tl.sb.append(fieldName);
		tl.sb.append(" ");
	}

	public FieldToken(Field f, Mapping m) {
		this.f = f;
                this.m = m;
	}

}
