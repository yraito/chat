package webchat.client.blocking;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager {

    final List<EventListener> els = Collections.synchronizedList(new LinkedList<>());
    final ExecutorService es = Executors.newSingleThreadExecutor();

    public void addListener(EventListener el) {
        es.submit(() -> {
            if (!els.contains(el)) {
                els.add(el);
            }
        });

    }

    public void removeListener(EventListener el) {
        es.submit(() -> {
            els.remove(el);
        });

    }

    public void dispatch(Event e) {
        es.submit(() -> {
            for (EventListener el : els) {
                el.onEvent(e);
            }
        });
    }

}
