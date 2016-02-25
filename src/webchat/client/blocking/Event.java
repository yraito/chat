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
public class Event {
    
    protected BlockingSession session;
    protected BlockingRoom room;
    protected long timestamp;

    public Event(BlockingSession session, long timestamp) {
        this.session = session;
        this.timestamp = timestamp;
    }

    public Event(BlockingSession session, BlockingRoom room, long timestamp) {
        this.session = session;
        this.room = room;
        this.timestamp = timestamp;
    }
    
    public BlockingSession getSession() {
        return session;
    }
    
    public BlockingRoom getRoom() {
        return room;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    
}
