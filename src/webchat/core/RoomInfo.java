package webchat.core;

public class RoomInfo {

    final String name;
    final String owner;
    final boolean isPrivateRoom;
    final int numberOfUsers;

    public RoomInfo(RoomBean r) {
        this.name = r.getName();
        this.owner = r.getOwner();
        this.isPrivateRoom = r.isPrivate();
        this.numberOfUsers = r.listUsers().size();
    }

    public RoomInfo(String name, String owner, boolean isPrivate, int numberOfUsers) {
        this.name = name;
        this.owner = owner;
        this.isPrivateRoom = isPrivate;
        this.numberOfUsers = numberOfUsers;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public boolean getIsPrivateRoom() {
        return isPrivateRoom;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

}
