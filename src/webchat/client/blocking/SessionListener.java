/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.blocking;
import webchat.core.*;
/**
 *
 * @author Nick
 */
public class SessionListener implements EventListener{
        @Override
    public void onEvent(Event e) {
        if (e instanceof SessionEvent) {
            SessionEvent se = (SessionEvent) e;
            BlockingSession sess = se.getSession();
            BlockingRoom room = se.getRoom();
            switch(se.getEventType()) {
                case JOIN_ROOM:
                    onJoinRoom(sess, room);
                    break;
                case LEAVE_ROOM:
                    onLeaveRoom(sess, room);
                    break;
                case CHANGE_STATUS:
                    onStatusChange(sess, sess.getUserStatus());
                    break;
                case CLOSE_SESSION:
                    onDisconnect(sess);
                    break;
            }
        }
    }
    
    public void onJoinRoom(BlockingSession sess, BlockingRoom room) {  }
    
    public void onLeaveRoom(BlockingSession sess, BlockingRoom channel) {  }
    
    public void onStatusChange(BlockingSession sess, UserStatus status) { }
    
    public void onDisconnect(BlockingSession sess) { }


}
