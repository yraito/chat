package webchat.client.blocking;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Closeable;
import webchat.client.ChatFuture;

import webchat.core.*;
import webchat.core.command.CreateCommand;
import webchat.core.command.JoinCommand;
import webchat.core.command.ListRoomsCommand;
import webchat.core.command.StatusCommand;
import webchat.core.command.LeaveCommand;
import webchat.core.command.DestroyCommand;
import webchat.client.MessageChannel;
import webchat.core.command.LoginCommand;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class BlockingSession implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(BlockingSession.class);

    private static class NullRetryStrategy implements RetryStrategy {

        @Override
        public long getTimeUntilNextTry(CommandMessage cmdMsg, int triesSoFar) {
            return -1;
        }
    }

    private String userName;
    private String userPass;
    private String userUuid;
    private final MessageChannel wrappedSess;
    private final EventManager eventManager;
    private RetryStrategy retryStrategy = new NullRetryStrategy();
    private final Map<String, BlockingRoom> openRooms = new ConcurrentHashMap<>();
    private UserStatus userStatus;

    public BlockingSession(MessageChannel channel, EventManager eventManager) {
        this.wrappedSess = channel;
        this.eventManager = eventManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public synchronized RetryStrategy getRetryStrategy() {
        return this.retryStrategy;
    }

    public synchronized void setRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public synchronized String getUsername() {
        return userName;
    }

    public synchronized String getPassword() {
        return userPass;
    }

    public synchronized String getUuid() {
        return userUuid;
    }

    public synchronized UserStatus getUserStatus() {
        return userStatus;
    }

    public Collection<BlockingRoom> getOpenRooms() {
        return openRooms.values();
    }

    public BlockingRoom getOpenRoom(String roomName) {
        return openRooms.get(roomName.toLowerCase());
    }

    public synchronized void login(String username, String password, String uuid) throws IOException, ChatException {
        LoginCommand loginCmd = new LoginCommand(username, password, uuid);
        writeRead(loginCmd);
        this.userName = username;
        this.userPass = password;
        this.userUuid = uuid;
    }

    public synchronized void changeStatus(UserStatus us) throws IOException, ChatException {
        writeRead(new StatusCommand(us));
        userStatus = us;

    }

    public List<RoomInfo> getRoomList() throws IOException, ChatException {
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

    public BlockingRoom joinRoom(String room) throws IOException, ChatException {
        return createBlockingRoom(new JoinCommand(room), null);
    }

    public BlockingRoom joinRoom(String room, String password) throws IOException, ChatException {
        return createBlockingRoom(new JoinCommand(room, password), null);
    }

    public BlockingRoom createRoom(String room) throws IOException, ChatException {
        return createBlockingRoom(new CreateCommand(room), userName);
    }

    public BlockingRoom createRoom(String room, String password) throws IOException, ChatException {
        return createBlockingRoom(new CreateCommand(room, password), userName);
    }

    public void leaveRoom(String room) throws IOException, ChatException {
        writeRead(new LeaveCommand(room));
        BlockingRoom br = openRooms.remove(room.toLowerCase());
        if (br != null) {
            br.close();
        }
    }

    public void destroyRoom(String room) throws IOException, ChatException {
        writeRead(new DestroyCommand(room));
        BlockingRoom br = openRooms.remove(room.toLowerCase());
        if (br != null) {
            br.close();
        }
    }

    public void close() {
        for (BlockingRoom room : getOpenRooms()) {
            try {
                leaveRoom(room.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        wrappedSess.close();
    }

    public ResultMessage writeReadNoRetry(CommandMessage cm) throws IOException, ChatException {
        ChatFuture<ResultMessage> futureResp = wrappedSess.writeFuture(cm);
        ResultMessage rm = futureResp.getUninterruptibly();
        if (rm.isError()) {
            boolean auth = rm.getErrorCode() == ResultMessage.ErrorCode.NOT_LOGGED_IN;
            auth |= rm.getErrorCode() == ResultMessage.ErrorCode.BAD_CREDENTIALS;
            String errMsg = rm.getError();
            if (auth) {
                throw new AuthException(errMsg);
            } else {
                throw new ChatException(rm.getError());
            }
        }
        return rm;
    }

    public ResultMessage writeRead(CommandMessage cm) throws IOException, ChatException {
        int tries = 0;
        while (true) {
            IOException ioe = null;
            try {
                ++tries;
                return writeReadNoRetry(cm);
            } catch (AuthException e) {
                ioe = e;
            } catch (IOException e) {
                ioe = e;
            }

            long waitTimeMs = this.retryStrategy.getTimeUntilNextTry(cm, tries);
            if (waitTimeMs < 0) {
                logger.error("Not retrying command");
                throw ioe;
            } else {
                logger.error("Error performing command {} : {}. Retrying ", cm, ioe.getMessage());
            }
            try {
                if (waitTimeMs > 0) {
                    Thread.sleep(waitTimeMs);
                }
            } catch (InterruptedException e) {
                logger.debug("interrupted");
            }
        }
    }

    private BlockingRoom createBlockingRoom(CommandMessage cmdMsg, String owner) throws IOException, ChatException {
        BlockingRoom blockingRoom = BlockingRoom.create(this, cmdMsg.getRoomName(), owner);
        writeRead(cmdMsg);
        openRooms.put(cmdMsg.getRoomName().toLowerCase(), blockingRoom);
        return blockingRoom;
    }

}
