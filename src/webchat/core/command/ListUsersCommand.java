package webchat.core.command;

import static webchat.core.Checks.*;

import java.util.Collection;
import java.util.LinkedList;
import webchat.core.ChatException;
import webchat.core.ChatManager;

import webchat.core.UserStatus;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.UserInfo;
import static webchat.util.StringUtils.isNullOrEmpty;

/**
 * Get a list of all users in room
 * @author Nick
 */
public class ListUsersCommand extends CommandMessage {

    public ListUsersCommand(String room) {
        super("listusers", null, room, null);
        super.persistable = false;
    }

    public ListUsersCommand() {
        super.persistable = false;
    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        String srcName = cs.getUserName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such room");
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Src not in room");
            Collection<String> users = mgr.getRoom(roomNameLower).listUsers();
            LinkedList<UserInfo> uis = new LinkedList<>();
            for (String user : users) {
                UserStatus stat = mgr.getUserStatus(user);
                UserInfo ui = new UserInfo(user, stat);
                uis.add(ui);
            }
            return ResultMessage.success(uis);
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
    }
}
