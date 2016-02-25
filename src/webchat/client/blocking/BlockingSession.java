package webchat.client.blocking;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import webchat.client.ChatFuture;

import webchat.client.ChatSession;
import webchat.core.*;
import webchat.core.command.CreateCommand;
import webchat.core.command.JoinCommand;
import webchat.core.command.ListRoomsCommand;
import webchat.core.command.StatusCommand;
import webchat.core.command.LeaveCommand;
import webchat.core.command.DestroyCommand;

public class BlockingSession {

    private final String userName;
    private final String userPass;
    ChatSession wrappedSess;
    private final EventManager eventManager;
    private final Map<String, BlockingRoom> openRooms = new ConcurrentHashMap<>();
    private UserStatus userStatus;

    public BlockingSession(String userName, String userPass, ChatSession channel, EventManager eventManager) {
        this.userName = userName;
        this.userPass = userPass;
        this.wrappedSess = channel;
        this.eventManager = eventManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public String getUsername() {
        return userName;
    }

    public String getPassword() {
        return userPass;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public Collection<BlockingRoom> getOpenRooms() {
        return openRooms.values();
    }

    public BlockingRoom getOpenRoom(String roomName) {
        return openRooms.get(roomName.toLowerCase());
    }

    public void changeStatus(UserStatus us) throws IOException, ChatException {
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
        return createBlockingRoom(new JoinCommand(room));
    }

    public BlockingRoom joinRoom(String room, String password) throws IOException, ChatException {
        return createBlockingRoom(new JoinCommand(room, password));
    }

    public BlockingRoom createRoom(String room) throws IOException, ChatException {
        return createBlockingRoom(new CreateCommand(room));
    }

    public BlockingRoom createRoom(String room, String password) throws IOException, ChatException {
        return createBlockingRoom(new CreateCommand(room, password));
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

    public ResultMessage writeRead(CommandMessage cm) throws IOException, ChatException {
        ChatFuture<ResultMessage> futureResp = wrappedSess.writeFuture(cm);
        ResultMessage rm = futureResp.getUninterruptibly();
        if (rm.isError()) {
            throw new ChatException(rm.getError());
        }
        return rm;
    }

    private BlockingRoom createBlockingRoom(CommandMessage cmdMsg) throws IOException, ChatException {
        BlockingRoom blockingRoom = BlockingRoom.create(this, cmdMsg.getRoomName());
        writeRead(cmdMsg);
        openRooms.put(cmdMsg.getRoomName().toLowerCase(), blockingRoom);
        return blockingRoom;
    }
}
