package webchat.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomBean {

    
    private final String roomName;
    private final String roomPassword;
    private String owner;
    private final Set<String> tokenHolders = ConcurrentHashMap.newKeySet();
    private final Set<String> users = ConcurrentHashMap.newKeySet();
    private final List<String> sortedUsers = Collections.synchronizedList(new ArrayList<>());
    private final Random rand = new Random();
    //id
    //TODO synchronize for change visibility

    public RoomBean(String creator, String name, String password) {
        this.owner = creator;
        this.roomName = name;
        this.roomPassword = password;
        if (!this.isLobby() && creator != null) {
            this.addUser(creator);
        }
    }

    public RoomBean(String creator, String name) {
        this(creator, name, null);
    }

    public RoomBean(String name) {
        this(null, name, null);
    }
    
    public RoomBean(RoomBean that) {
        this.roomName = that.roomName;
        this.roomPassword = that.roomPassword;
        this.owner = that.owner;
        this.tokenHolders.addAll(that.tokenHolders);
        this.users.addAll(that.users);
    }

    public String getName() {
        return roomName;
    }

    public String getPassword() {
        return roomPassword;
    }

    public boolean isPrivate() {
        return roomPassword != null;
    }

    public boolean isLobby() {
        return roomName.equalsIgnoreCase("lobby");
    }

    public synchronized String getOwner() {
        return owner;
    }

    public synchronized void setOwner(String user) {
        if (user == null) {
            owner = null;
        } else {
             owner = user.toLowerCase();
        }
       
    }

    public Collection<String> getTokenHolders() {
        return tokenHolders;
    }

    public boolean giveToken(String user) {
        return tokenHolders.add(user.toLowerCase());
    }

    public boolean takeToken(String user) {
        return tokenHolders.remove(user.toLowerCase());
    }

    public Collection<String> listUsers() {
        return Collections.unmodifiableCollection(users);
    }

    public synchronized Collection<String> listUsers(boolean copy) {
        if (!copy) {
            return listUsers();
        }
        return new LinkedList<>(users);
    }

    public void addUser(String userName) {
        userName = userName.toLowerCase();
        if (!users.contains(userName)) {
            users.add(userName);
            sortedUsers.add(userName);
        }
        assert users.size() == sortedUsers.size();
    }

    public boolean removeUser(String userName) {
        userName = userName.toLowerCase();
        if (getOwner().equalsIgnoreCase(userName)) {
            setOwner(null);
        }
        tokenHolders.remove(userName);
        sortedUsers.remove(userName);
        return users.remove(userName);
    }

    public boolean hasUser(String userName) {
        return users.contains(userName.toLowerCase());
    }

    public String chooseRandomUser() {
        int num = rand.nextInt(sortedUsers.size());
        return sortedUsers.get(num);
    }

}
