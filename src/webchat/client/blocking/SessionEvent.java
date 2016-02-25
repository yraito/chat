/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.blocking;

/**
 *
 * @author Edward
 */
public class SessionEvent extends Event {

    public enum EventType {
        CLOSE_SESSION,
        JOIN_ROOM,
        LEAVE_ROOM,
        CHANGE_STATUS
    }

    EventType eventType;

    public SessionEvent(BlockingSession session, BlockingRoom room, EventType eventType) {
        super(session, room, System.currentTimeMillis());
        this.eventType = eventType;
    }

    public SessionEvent(BlockingSession session, EventType eventType) {
        this(session, null, eventType);
    }

    EventType getEventType() {
        return eventType;
    }

    
}
