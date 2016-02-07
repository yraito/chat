package webchat.client.blocking;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import webchat.client.ChatSession;
import webchat.core.*;
import webchat.core.command.CreateCommand;
import webchat.core.command.JoinCommand;
import webchat.core.command.ListRoomsCommand;

public class BlockingSession {

    public static <T> T waitFor(Future<T> future, long ctxTimeout) throws ConnectionException, ProtocolException {

        try {

            try {
                return future.get(ctxTimeout, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException | InterruptedException e) {
                throw new ConnectionException(e);
            }
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String userName;
    private String userPass;
    private ChatSession channel;
    private Map<String, BlockingChannel> openChannels = new HashMap<>();
    private List<SessionListener> sessListeners = new LinkedList<>();
    private EventManager eventManager;
    private long timeoutMs = 10000;

    public BlockingSession(String userName, String userPass, ChatSession channel, EventManager eventManager) {
        this.userName = userName;
        this.userPass = userPass;
        this.channel = channel;
        this.eventManager = eventManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public long getTimeout() {
        return timeoutMs;
    }

    public void setTimeout(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getUsername() {
        return userName;
    }

    public String getPassword() {
        return userPass;
    }

    public Collection<BlockingChannel> getOpenChannels() {
        return openChannels.values();
    }

    public List<RoomInfo> getAvailableChannels() throws IOException, ChatException {
        ResultMessage rm = writeRead(new ListRoomsCommand());
        if (!(rm.getResult() instanceof List)) {
            throw new ProtocolException("Expected list, but got " + rm.getResult().getClass());
        }
        List<?> msgList = (List<?>) rm.getResult();
        List<RoomInfo> rtrnList = new ArrayList<>();
        for (Object o : msgList) {
            if (!(o instanceof RoomInfo)) {
                throw new ProtocolException("Expected roominfo, got " + o.getClass());
            }
            rtrnList.add((RoomInfo) o);
        }
        return rtrnList;
    }

    public BlockingChannel openChannel(String room) throws IOException, ChatException {
        ResultMessage rm = writeRead(new JoinCommand(room));
        return makeChatChannel(rm);
    }

    public BlockingChannel openChannel(String room, String password) throws IOException, ChatException {
        ResultMessage rm = writeRead(new JoinCommand(room, password));
        return makeChatChannel(rm);
    }

    public BlockingChannel createChannel(String room) throws IOException, ChatException {
        ResultMessage rm = writeRead(new CreateCommand(room));
        return makeChatChannel(rm);
    }

    public BlockingChannel createChannel(String room, String password) throws IOException, ChatException {
        ResultMessage rm = writeRead(new CreateCommand(room, password));
        return makeChatChannel(rm);
    }

    public void disconnect() {
        channel.close();
    }

    private BlockingChannel makeChatChannel(ResultMessage resultMsg) throws ProtocolException {
        if (!(resultMsg.getResult() instanceof RoomBean)) {
            throw new ProtocolException("Expecting RoomBean, got " + resultMsg.getResult().getClass() + ": " + resultMsg);
        }
        RoomBean room = (RoomBean) resultMsg.getResult();
        BlockingChannel channel = new BlockingChannel();
        channel.init(room, this);
        notifyJoin(channel);
        return channel;

    }

    public ResultMessage writeRead(CommandMessage cm) throws IOException, ChatException {
        Future<ResultMessage> futureResp = channel.writeFuture(cm, null);
        ResultMessage rm = waitFor(futureResp, timeoutMs);
        if (rm.isError()) {
            throw new ChatException(rm.getError());
        }
        return rm;
    }

    public void addListener(SessionListener sl) {
        if (!sessListeners.contains(sl)) {
            sessListeners.add(sl);
        }
    }

    public void removeListener(SessionListener sl) {
        sessListeners.remove(sl);
    }

    public void notifyJoin(BlockingChannel ch) {
        for (SessionListener sl : sessListeners) {
            sl.onJoinChannel(ch);
        }
    }

    public void notifyLeave(BlockingChannel ch) {
        for (SessionListener sl : sessListeners) {
            sl.onLeaveChannel(ch);
        }
    }

}
