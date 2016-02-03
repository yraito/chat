package webchat.dao.dto;

public interface AccountRecord {

	Integer getId();
	
	String getUsername();
	
	String getPasshash();
	
	Long getCreated();
}
