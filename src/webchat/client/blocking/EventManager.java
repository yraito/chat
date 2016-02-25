package webchat.client.blocking;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventManager {

        List<EventListener> els = Collections.synchronizedList(new LinkedList<>());
        
	public void addListener(EventListener el) {
            if (!els.contains(el)) {
                els.add(el);
            }
        }
	
	void removeListener(EventListener el) {
            els.remove(el);
        }
        
        void dispatch(Event e) {
            for (EventListener el : els) {
                el.onEvent(e);
            }
        }
	
}
