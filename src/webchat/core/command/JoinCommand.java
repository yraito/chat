package webchat.core.command;

import static webchat.core.Checks.*;

import java.util.LinkedList;
import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomBean;
import webchat.core.RoomSnapshot;
import static webchat.util.StringUtils.isNullOrEmpty;

/**
 * Join a public or private room
 * @author Nick
 */
public class JoinCommand extends CommandMessage {

    public JoinCommand(String rm, String password) {
        super("join", null, rm, null, password);
    }

    public JoinCommand(String rm) {
        super("join", null, rm, null);
    }

    public JoinCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {

        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        String srcName = cs.getUserName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such rooom");
            boolean srcInRoom = userIsInRoom(mgr, srcName, roomNameLower);
            if (!srcInRoom) {
                String pass0 = getOtherArgs().size() == 1 ? getArg(0) : null;
                String pass1 = mgr.getRoom(roomNameLower).getPassword();
                checkAuthorized(pass1 == null || pass1.equals(pass0), "Incorrect password");
            }

            RoomBean room = mgr.getRoom(roomNameLower);
            RoomSnapshot roomSnapshot = new RoomSnapshot(mgr, room);
            if (!srcInRoom) {
                room.addUser(srcName);
                clearOtherArgs();
                mgr.dispatchMessage(this, roomNameLower);
            }
            return ResultMessage.success(roomSnapshot);
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }

    }

}
