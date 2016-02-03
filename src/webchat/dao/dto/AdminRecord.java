package webchat.dao.dto;


@Table(name = "Admins")
public class AdminRecord implements AccountRecord{

	@Column(name = "Id", isPrimaryKey = true)
	Integer id;
	
	@Column(name = "Username")
	String username;
	
	@Column(name = "Passhash")
	String passhash;
	
	@Column(name = "Email")
	String email;
	
	@Column(name = "Created")
	Long created;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasshash() {
		return passhash;
	}

	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "[" + id + ", " + username + ", " + passhash + ", " 
                        + ", " + email + ", " + created + "]";
	}
	
	
	
}
