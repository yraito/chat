package webchat.core;

import webchat.core.UserStatus;

/**
 * 
 * @author dsotm3200
 *
 */
public class UserInfo {

	String username;
	UserStatus status = UserStatus.ONLINE;
	
	public UserInfo(String username) {
		super();
		this.username = username;
	}
	
	public UserInfo(String username, UserStatus status) {
		super();
		this.username = username;
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserInfo) {
			UserInfo that = (UserInfo) obj;
			return that.username.equalsIgnoreCase(username);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public String toString() {
		return username + " [" + status + "]";
	}



	
}
