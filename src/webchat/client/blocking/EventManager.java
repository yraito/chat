package webchat.client.blocking;

import webchat.core.*;

public interface EventManager {
        
	void addListener(String room, ChannelListener el);
	
	void removeListener(String room, ChannelListener el);
        
        void removeListeners(String room);
	
}
