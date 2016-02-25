/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.blocking;

import webchat.core.*;

/**
 *
 * @author Edward
 */
public class MessageEvent extends Event {

    CommandMessage message;

    public MessageEvent(BlockingRoom room, CommandMessage message) {
        super(room.getSession(), room, message.getTimeStamp());
        this.message = message;
    }

    public MessageEvent(BlockingSession sess, CommandMessage message) {
        super(sess, message.getTimeStamp());
        this.message = message;
    }

    public CommandMessage getMessage() {
        return message;
    }
}
