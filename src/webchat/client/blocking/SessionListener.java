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
public class SessionListener implements EventListener {


    @Override
    public void onEvent(Event e) {
        if (e instanceof MessageEvent) {
            MessageEvent se = (MessageEvent) e;
            CommandMessage cmdMsg = se.getMessage();
            BlockingSession sess = se.getSession();
            BlockingRoom room = se.getRoom();
            if (cmdMsg.getSourceName().equalsIgnoreCase(sess.getUsername())) {
                switch (cmdMsg.getCommand().toLowerCase()) {
                    case "join":
                        onJoinRoom(sess, room);
                        break;
                    case "login":
                        onLogin(sess);
                        break;
                    case "logout":
                        onDisconnect(sess);
                        break;
                    case "leave":
                        onLeaveRoom(sess, room);
                        break;
                    case "status":
                        UserStatus us = UserStatus.fromString(cmdMsg.getArg(0));
                        onStatusChange(sess, us);
                        break;
                }
            } else if (cmdMsg.getTargetName().equalsIgnoreCase(sess.getUsername())) {
                if (cmdMsg.getCommand().equalsIgnoreCase("kick")) {
                    onKickedFromRoom(sess, room, cmdMsg.getMessage());
                }
            }

        }
    }

    public void onLogin(BlockingSession sess) { }

    public void onJoinRoom(BlockingSession sess, BlockingRoom room) { }

    public void onLeaveRoom(BlockingSession sess, BlockingRoom channel) { }
    
    public void onKickedFromRoom(BlockingSession sess, BlockingRoom channel, String reason) { }

    public void onStatusChange(BlockingSession sess, UserStatus status) { }

    public void onDisconnect(BlockingSession sess) { }

}
