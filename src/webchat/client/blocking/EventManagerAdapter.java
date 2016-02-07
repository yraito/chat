package webchat.client.blocking;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.client.ChatSession;
import webchat.core.CommandMessage;
import webchat.core.Message;
import webchat.core.MessageListMessage;
import webchat.client.*;

public class EventManagerAdapter implements EventManager, ChatHandler {

    private final static Logger logger = LoggerFactory.getLogger(EventManagerAdapter.class);

    private Map<String, List<ChannelListener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(ChatSession ioc, Message cm) {
        logger.debug("onMessageReceived: {}", cm);

        if (cm instanceof CommandMessage) {
            dispatch0((CommandMessage) cm);
        } else if (cm instanceof MessageListMessage) {
            for (Message m : ((MessageListMessage) cm).getMessages()) {
                if (m instanceof CommandMessage) {
                    dispatch0((CommandMessage) cm);
                }
            }
        }
    }

    private void dispatch0(CommandMessage cmdMsg) {
        String roomNameLower = cmdMsg.getRoomName().toLowerCase();
        List<ChannelListener> cls = this.listeners.get(roomNameLower);
        if (cls != null) {
            for (ChannelListener cl : cls) {
                dispatch1(cmdMsg, cl);
            }
        }
    }

    private void dispatch1(CommandMessage cm, ChannelListener rl) {
        switch (cm.getCommand()) {
            case "join":
                rl.onJoin(cm.getSourceName());
                break;
            case "leave":
                String newOwnr = cm.getOtherArgs().size() > 0 ? cm.getArg(0) : null;
                rl.onLeave(cm.getSourceName(), newOwnr);
                break;
            case "message":
                rl.onMessage(cm.getSourceName(), cm.getMessage());
                break;
            case "whisper":
                rl.onWhisper(cm.getSourceName(), cm.getMessage());
                break;
            case "kick":
                rl.onKick(cm.getSourceName(), cm.getTargetName(), cm.getMessage());
                break;
            case "grant":
                rl.onGrantToken(cm.getTargetName());
                break;
            case "revoke":
                rl.onRevokeToken(cm.getTargetName());
                break;
            default:
                logger.warn("Unrecognized command: {}", cm.getCommand());
        }
    }

    @Override
    public void addListener(String roomName, ChannelListener el) {
        String roomNameLower = roomName.toLowerCase();
        List<ChannelListener> cls = this.listeners.get(roomNameLower);
        if (cls == null) {
            cls = new LinkedList<>();
            this.listeners.put(roomNameLower, cls);
        }
        if (!cls.contains(el)) {
            cls.add(el);
        }
    }

    @Override
    public void removeListener(String roomName, ChannelListener el) {
        String roomNameLower = roomName.toLowerCase();
        List<ChannelListener> cls = this.listeners.get(roomNameLower);
        if (cls != null) {
            cls.remove(el);
        }
    }
    
    public void removeListeners(String roomName) {
        this.listeners.remove(roomName.toLowerCase());
    }

}
