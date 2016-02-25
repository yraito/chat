package webchat.core.command;

import static webchat.core.Checks.*;
import static webchat.util.StringUtils.*;

import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;

/**
 * Kick user from room
 *
 * @author Nick
 */
public class KickCommand extends CommandMessage {

    public KickCommand(String target, String room, String reason) {
        super("kick", target, room, reason);
    }

    public KickCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        checkArgs(!isNullOrEmpty(getTargetName()), "missing target arg");
        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        checkArgs(!isNullOrEmpty(getMessage()), "missing reason arg");

        String srcName = cs.getUserName();
        String tgtName = getTargetName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such room");
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Src not in room");
            checkState(userIsInRoom(mgr, tgtName, roomNameLower), "Tgt not in room");
            checkState(userHasPower(mgr, srcName, roomNameLower), "Don't have owner privs");
            checkState(!srcName.equalsIgnoreCase(tgtName), "Can't self-kick");
            
            mgr.dispatchMessage(this, roomNameLower);
            mgr.getRoom(roomNameLower).removeUser(tgtName);
            return ResultMessage.success();
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
    }

}
