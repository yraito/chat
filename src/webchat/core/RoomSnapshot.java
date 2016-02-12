/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Edward
 */
public class RoomSnapshot {
    
    public enum RoomPrivs {
        OWNER, TOKEN, NONE
    }
    
    public static class RoomUser {
        
        String name;
        RoomPrivs privs;
        UserStatus state;
        
        RoomUser(String name, RoomPrivs privs, UserStatus state) {
            this.name = name;
            this.privs = privs;
            this.state = state;
        }
    }
    
    public String name;
    public String password;
    public List<RoomUser> users = new ArrayList<>();
    
    public RoomSnapshot(ChatManager mgr, RoomBean room) {
        this.name = room.getName();
        this.password = room.getPassword();
        for (String user : room.listUsers()) {
            UserStatus state = mgr.getUserStatus(user);
            RoomPrivs privs = null;
            if (room.getOwner().equalsIgnoreCase(user)) {
                privs = RoomPrivs.OWNER;
            } else if (room.getTokenHolders().contains(user)) {
                privs = RoomPrivs.TOKEN;
            } else {
                privs = RoomPrivs.NONE;
            }
            this.users.add((new RoomUser(user, privs, state)));
        }
    }
}
