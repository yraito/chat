package webchat.core.command;

import static webchat.core.Checks.*;
import static webchat.util.StringUtils.*;

import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;

/**
 * Destroy a room
 *
 * @author Nick
 */
public class DestroyCommand extends CommandMessage {

    public DestroyCommand(String roomName, String reason) {
        super("destroy", null, roomName, reason);
    }

    public DestroyCommand(String roomName) {
        super("destroy", null, roomName, "Hulk smash");
    }

    public DestroyCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {

        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");

        String srcName = cs.getUserName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);

            checkState(roomExists(mgr, roomNameLower), "No such rooom");
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Not in room");
            //Can token holder destroy room?
            checkAuthorized(userHasPower(mgr, srcName, roomNameLower), "Don't have owner privs");
            checkState(!mgr.getRoom(roomNameLower).isLobby(), "Can't destroy lobby");

            mgr.dispatchMessage(this, roomNameLower);
            mgr.removeRoom(roomNameLower);
            return ResultMessage.success();
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
        
    }

}
