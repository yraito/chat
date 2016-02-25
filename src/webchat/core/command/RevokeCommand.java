package webchat.core.command;

import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomBean;
import static webchat.core.Checks.*;
import static webchat.util.StringUtils.isNullOrEmpty;

/**
 * Revoke a room-ownership token
 * @author Nick
 */
public class RevokeCommand extends CommandMessage {

    public RevokeCommand(String tgt, String rm) {
        super("revoke", tgt, rm, null);
    }

    public RevokeCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        checkArgs(!isNullOrEmpty(getTargetName()), "missing target arg");
        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        String srcName = cs.getUserName();
        String granteeName = getTargetName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such rooom");
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Src not in room");
            checkState(userIsInRoom(mgr, granteeName, roomNameLower), "Tgt not in room");
            RoomBean r = mgr.getRoom(roomNameLower);
            checkAuthorized(r.getOwner().equalsIgnoreCase(srcName), "Not owner");
            checkState(!srcName.equalsIgnoreCase(granteeName), "Can't self-revoke");

            if (r.takeToken(granteeName)) {
                mgr.dispatchMessage(this, roomNameLower);
            }
            return ResultMessage.success();

        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
    }

}
