package webchat.core;

import webchat.core.command.LeaveCommand;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.core.command.LoginCommand;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.util.LockManager;

import static webchat.util.Util.*;

/**
 * The central class of the chat server.
 *
 * @author Nick
 */
public class ChatManager {

    private final static Logger logger = LoggerFactory.getLogger(ChatManager.class);

    private final Map<String, RoomBean> chatRooms = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> onlineUsers = new ConcurrentHashMap<>();
    private final Map<String, UserStatus> userStatuses = new ConcurrentHashMap<>();
    private final LockManager lockManager = new LockManager();
    private final DaoConnectionFactory daoFactory;

    public ChatManager(DaoConnectionFactory daoFactory) {
        super();
        this.daoFactory = daoFactory;
        //Create the lobby room
        logger.info("Creating ChatManager with DaoConnectionFactory {}", daoFactory);
        addRoom(new RoomBean("server", "Lobby"));
    }

    /**
     * Handle a CommandMessage
     *
     * @param clientSess
     * @param commandMsg
     * @return
     */
    public ResultMessage processMessage(ClientSession clientSess, CommandMessage commandMsg) {

        logger.debug("Processing command: {}, {}", clientSess.getUserName(), commandMsg);
        String srcName = clientSess.getUserName();
        if (srcName == null && !(commandMsg instanceof LoginCommand)) {
            logger.debug("User not logged in, can't \"{}\". Responding error", commandMsg.getCommand());
            return ResultMessage.error("Not logged in");
        }

        ResultMessage resultMsg = null;
        DaoConnection daoConnection = null;
        Thread.currentThread().setName(srcName + Math.random() * 1000000);
        try {
            commandMsg.setSourceName(srcName);
            commandMsg.setTimeStamp(System.currentTimeMillis());
            logger.debug("Executing command: {}", commandMsg);
            resultMsg = commandMsg.execute(clientSess, this);

            if (srcName == null && !clientSess.isClosed()) {
                commandMsg.setSourceName(clientSess.getUserName());
            }
            if (!resultMsg.isError()) {
                daoConnection = daoFactory.openDaoConnection();
                logger.debug("Persisting command: {} over DaoConnectino: {}", commandMsg, daoConnection);
                commandMsg.persist(clientSess.getUserId(), daoConnection, this);
            }

            // String srcNameLower = srcName.toLowerCase();
            //onlineUsers.put(srcNameLower, clientSess);
        } catch (ChatException e) {
            logger.info("ChatException: {}", e.getMessage());
            resultMsg = ResultMessage.error(e.getMessage());
        } catch (DaoException e) {
            logger.error("DaoException: {}, {}", e.getMessage(), e.getCause());
            if (resultMsg == null) {
                resultMsg = ResultMessage.error(e.getMessage());
            }
        } finally {
            closeQuietly(daoConnection);
        }
        assert resultMsg != null;
        return resultMsg;

    }

    /**
     * Should be invoked when a new session is created
     *
     * @param cs
     */
    public void onNewSession(ClientSession cs) {
        logger.debug("onNewSession: {}", cs.getUserName());
        String userName = cs.getUserName().toLowerCase();
        onlineUsers.put(userName, cs);
        userStatuses.put(userName, UserStatus.ONLINE);
    }

    /**
     * Should be invoked when a session is destroyed
     *
     * @param cs
     */
    public void onEndSession(ClientSession cs) {
        logger.debug("onEndSession: {}", cs.getUserName());
        //null pointer
        String userName = cs.getUserName().toLowerCase();
        for (RoomBean r : chatRooms.values()) {
            String roomName = r.getName();
            String roomNameLower = roomName.toLowerCase();
            lockManager.acquireLock(roomNameLower);
            if (r.removeUser(userName)) {
                LeaveCommand lc = new LeaveCommand(roomName);
                try {
                    lc.execute(cs, this);
                } catch (ChatException e) {
                    e.printStackTrace();
                }
            }
            lockManager.releaseLock(roomNameLower);
        }
        onlineUsers.remove(userName);
        userStatuses.remove(userName);
    }

    /**
     * Find room by name
     *
     * @param room
     * @return
     */
    public synchronized RoomBean getRoom(String room) {
        return chatRooms.get(room.toLowerCase());
    }

    /**
     * Get a list of all rooms
     *
     * @return
     */
    public synchronized Collection<RoomBean> getRooms() {
        return Collections.unmodifiableCollection(chatRooms.values());
        //make immutable
    }

    
    /**
     * Get a list of RoomInfos, abridged versions of RoommBeans
     *
     * @return
     */
    public Collection<RoomInfo> listRoomInfos() {
        return getRooms().stream()
                .map((r) -> (new RoomInfo(r)))
                .collect(Collectors.toList());
    }

    public synchronized void addRoom(RoomBean r) {
        logger.debug("Adding room {}", r);
        chatRooms.put(r.getName().toLowerCase(), r);
    }

    public synchronized void removeRoom(RoomBean r) {
        logger.debug("Removing room {}", r);
        chatRooms.remove(r.getName().toLowerCase());
    }

    public synchronized void removeRoom(String name) {
        logger.debug("Removing room {}", name);
        chatRooms.remove(name.toLowerCase());
    }

    public synchronized ClientSession getClientSession(String userName) {
        return onlineUsers.get(userName.toLowerCase());
    }

    public synchronized UserStatus getUserStatus(String username) {
        username = username.toLowerCase();
        if (!onlineUsers.containsKey(username)) {
            return UserStatus.OFFLINE;
        }
        UserStatus status = userStatuses.get(username);
        if (status == null) {
            status = UserStatus.ONLINE;
        }
        return status;
    }

    public synchronized void setUserStatus(String username, UserStatus status) {
        logger.debug("Setting status of {} to {}", username, status.getString());
        username = username.toLowerCase();
        userStatuses.put(username, status);
    }

    public LockManager getLockManager() {
        return lockManager;
    }

    public DaoConnectionFactory getDaoFactory() {
        return daoFactory;
    }

    /**
     * Send a message to all users in the room
     *
     * @param msg
     * @param room
     */
    public void dispatchMessage(CommandMessage msg, String room, ClientSession ignoreCs) {
        logger.debug("Dispatching to room {} message {}", room, msg);
        RoomBean rmBean = getRoom(room);
        logger.debug("Found room {}", rmBean);
        Collection<String> users = rmBean.listUsers();
        logger.debug("Sending message to users in room: {}", users);
        for (String user : users) {
            ClientSession cs = onlineUsers.get(user.toLowerCase());
            if (ignoreCs != null && user.equalsIgnoreCase(ignoreCs.getUserName())) {
                logger.debug("Skipping dispatch to {}", user);
                continue;
            }
            if (cs == null) {
                logger.warn("null ClientSession for user {}, dispatching {}",  user, msg);
                continue;
            }
            try {
                logger.debug("Writing to ClientSession {}", cs);
                cs.writeMessage(msg);
            } catch (IOException e) {
                logger.error("Error writing message to {}: {}", cs.getUserName(), e.getMessage());
            }
        }
    }

    /**
     * Send a message to a single user
     *
     * @param msg
     * @param cs
     */
    public void dispatchMessage(CommandMessage msg, ClientSession cs) {
        logger.debug("Dispatching to user {} message {}", cs.getUserName(), msg);
        try {
            cs.writeMessage(msg);
        } catch (IOException e) {
            logger.error("Error writing message to {}: {}", cs.getUserName(), e.getMessage());
        }
    }

    public void dispatchMessage(CommandMessage msg, String room) {
        dispatchMessage(msg, room, null);
    }
}
