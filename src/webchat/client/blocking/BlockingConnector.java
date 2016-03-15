package webchat.client.blocking;

import java.io.IOException;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import webchat.client.ChatCallback;
import webchat.client.ChatFuture;
import webchat.client.ChatHandler;

import webchat.core.CommandMessage;
import webchat.core.Message;
import webchat.util.Util;
import webchat.client.MessageChannel;
import webchat.client.ChannelFactory;

public class BlockingConnector {

    private final ChannelFactory channelFactory;

    public BlockingConnector(ChannelFactory iocfact) {
        this.channelFactory = iocfact;
    }

    public BlockingSession open() throws IOException {
        EventManager eventMgr = new EventManager();
        EventDispatchingChatHandler chatHandler = new EventDispatchingChatHandler(eventMgr);
        ChatFuture<MessageChannel> futureSession = channelFactory.open(chatHandler);
        futureSession.getUninterruptibly();
        return chatHandler.blockingSess.get();
    }

    private class EventDispatchingChatHandler implements ChatHandler {

        final EventManager eventMgr;
        final AtomicReference<BlockingSession> blockingSess = new AtomicReference<>();

        public EventDispatchingChatHandler(EventManager eventMgr) {
            this.eventMgr = eventMgr;
        }
        
        @Override
        public synchronized void onChannelOpened(MessageChannel chatSess) {
            blockingSess.set(new BlockingSession(chatSess, eventMgr));
        }
        
        @Override
        public synchronized void onMessageReceived(MessageChannel chatSess, Message m) {
            if (m instanceof CommandMessage) {
                CommandMessage cmdMsg = (CommandMessage) m;
                eventMgr.dispatch(new MessageEvent(blockingSess.get(), cmdMsg));
            }
        }
    }

}
