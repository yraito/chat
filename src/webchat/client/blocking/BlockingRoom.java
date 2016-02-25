package webchat.client.blocking;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import webchat.core.*;
import webchat.core.command.*;
import static webchat.core.RoomSnapshot.RoomPrivs;

/**
 * Client's interface to a room he is in.
 *
 * @author Nick
 *
 */
public class BlockingRoom {

    //prevent escaping this
    public static BlockingRoom create(BlockingSession sess, String roomName) {
        BlockingRoom room = new BlockingRoom(sess, roomName);
        room.name = roomName;
        //sess.getEventManager().addListener(room.updatingListener);
        return room;
    }
    
    private RoomBean roomBean;
    private String name;
    private final Map<String, UserStatus> statuses = new ConcurrentHashMap<>();
    private final BlockingSession sess;
    private final UpdatingListener updatingListener;

    private BlockingRoom(BlockingSession sess, String roomName) {
        this.sess = sess;
        this.updatingListener = new UpdatingListener(roomName);
    }

    private void init(RoomSnapshot roomSnapshot) {
        this.roomBean = new RoomBean(roomSnapshot.name, roomSnapshot.password);
        for (RoomSnapshot.RoomUser user : roomSnapshot.users) {
            roomBean.addUser(user.name);
            statuses.put(user.name.toLowerCase(), user.state);
            if (user.privs == RoomPrivs.OWNER) {
                roomBean.setOwner(user.name);
            } else if (user.privs == RoomPrivs.TOKEN) {
                roomBean.giveToken(user.name);
            }
        }
    }

    public BlockingSession getSession() {
        return sess;
    }

    public String getName() {
       // return roomBean.getName();
       return name;
    }

    public String getPassword() {
        return roomBean.getPassword();
    }

    public String getOwner() {
        return roomBean.getOwner();
    }

    public Collection<String> listUsers() {
        return roomBean.listUsers();
    }

    public Collection<String> listTokenHolders() {
        return roomBean.getTokenHolders();
    }

    public void grantToken(String user) throws IOException, ChatException {
        sess.writeRead(new GrantCommand(user, getName()));
    }

    public void revokeToken(String user) throws IOException, ChatException {
        sess.writeRead(new RevokeCommand(user, getName()));
    }

    public void sendMessage(String message) throws IOException, ChatException {
        sess.writeRead(new MessageCommand(getName(), message));
    }

    public void sendWhisper(String user, String message) throws IOException, ChatException {
        sess.writeRead(new WhisperCommand(user, getName(), message));
    }

    public void kickUser(String user, String reason) throws IOException, ChatException {
        sess.writeRead(new KickCommand(user, getName(), reason));
    }

    public void close() {
        sess.getEventManager().removeListener(updatingListener);
    }

    private class UpdatingListener extends RoomListener {

        UpdatingListener(String roomName) {
            super(roomName);
        }
        
        public void onRoomInfo(BlockingRoom room, RoomSnapshot rs) {
            init(rs);
        }
        
        public void onJoin(BlockingRoom room, String src) {
            roomBean.addUser(src);
        }

        public void onLeave(BlockingRoom room, String src) {
            roomBean.removeUser(src);
        }

        public void onKick(BlockingRoom room, String src, String tgt, String rsn) {
            roomBean.removeUser(tgt);
        }

        public void onGrantToken(BlockingRoom room, String tgt) {
            roomBean.giveToken(tgt);
        }

        public void onRevokeToken(BlockingRoom room, String tgt) {
            roomBean.takeToken(tgt);
        }

        public void onNewOwner(BlockingRoom room, String tgt) {
            roomBean.setOwner(tgt);
        }

        public void onDestroy(BlockingRoom room, String rsn) {
            try {
                sess.destroyRoom(getName());
            } catch (IOException | ChatException e) {
                
            }
            
        }
        
        public void onStatusChange(String src, UserStatus status) {
            statuses.put(src.toLowerCase(), status);
        }
    }
}
