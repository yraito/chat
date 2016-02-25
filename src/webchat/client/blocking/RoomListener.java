package webchat.client.blocking;

import java.util.Objects;
import webchat.core.CommandMessage;
import webchat.core.RoomSnapshot;
import webchat.core.UserStatus;

/**
 * A subscriber of events in a chat room (aka "chat channel")
 *
 *
 */
public class RoomListener implements EventListener {

    String roomName;

    public RoomListener(String roomName) {
        this.roomName = roomName;
    }

    public RoomListener() {
    }
    
    @Override
    public void onEvent(Event e) {
        if (e instanceof MessageEvent) {
            MessageEvent me = (MessageEvent) e;
            CommandMessage cm = me.getMessage();
            if (roomName != null && !roomName.equalsIgnoreCase(cm.getRoomName())) {
                return;
            }
            BlockingSession sess = me.getSession();
            BlockingRoom room = null;
            if (cm.getRoomName() != null) {
                room = sess.getOpenRoom(cm.getRoomName());
            }
            switch (cm.getCommand()) {
                case "join":
                    if (cm.getSourceName().equalsIgnoreCase(sess.getUsername())) {
                        Object o = cm.getAttachment();
                        if (o == null || !(o instanceof RoomSnapshot)) {
                           System.out.println("Error... Missing room snapshot" + o);
                        } else {
                            onRoomInfo(room, (RoomSnapshot) o);
                        }
                    } 
                    onJoin(room, cm.getSourceName());
                    break;
                case "leave":
                    String newOwnr = cm.getOtherArgs().size() > 0 ? cm.getArg(0) : null;
                    onLeave(room, cm.getSourceName());
                    if (newOwnr != null) {
                        onNewOwner(room, newOwnr);
                    }
                    break;
                case "message":
                    onMessage(room, cm.getSourceName(), cm.getMessage());
                    break;
                case "whisper":
                    onWhisper(room, cm.getSourceName(), cm.getMessage());
                    break;
                case "kick":
                    onKick(room, cm.getSourceName(), cm.getTargetName(), cm.getMessage());
                    break;
                case "grant":
                    onGrantToken(room, cm.getTargetName());
                    break;
                case "revoke":
                    onRevokeToken(room, cm.getTargetName());
                    break;
                case "destroy":
                    onDestroy(room, cm.getMessage());
                    break;
                case "status":
                    UserStatus us = UserStatus.fromString(cm.getArg(0));
                    onStatusChange(cm.getSourceName(), us);
            }
        }

    }

    public void onRoomInfo(BlockingRoom room, RoomSnapshot rs) {
        
    }
    public void onJoin(BlockingRoom room, String src) {
    }

    public void onLeave(BlockingRoom room, String src) {
    }

    public void onMessage(BlockingRoom room, String src, String msg) {
    }

    public void onWhisper(BlockingRoom room, String src, String msg) {
    }

    public void onKick(BlockingRoom room, String src, String tgt, String rsn) {
    }

    public void onGrantToken(BlockingRoom room, String tgt) {
    }

    public void onRevokeToken(BlockingRoom room, String tgt) {
    }

    public void onNewOwner(BlockingRoom room, String tgt) {
    }

    public void onDestroy(BlockingRoom room, String rsn) {
    }

    public void onStatusChange(String src, UserStatus status) {
    }

}
