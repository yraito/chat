package webchat.core;

public class RoomInfo {

	final String name;
	final boolean isPrivate;
	final int numberOfUsers;
	
	public RoomInfo(RoomBean r) {
		this.name = r.getName();
		this.isPrivate = r.isPrivate();
		this.numberOfUsers = r.listUsers().size();
	}

	public String getName() {
		return name;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}
	
}
