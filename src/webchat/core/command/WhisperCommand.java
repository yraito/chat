package webchat.core.command;

import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import static webchat.core.Checks.*;
import static webchat.util.StringUtils.*;

/**
 * Send a whisper to another user
 *
 * @author Nick
 */
public class WhisperCommand extends CommandMessage {

    public WhisperCommand(String tgt, String rm, String msg) {
        super("whisper", tgt, rm, msg);
    }

    public WhisperCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        checkArgs(!isNullOrEmpty(getTargetName()), "missing target arg");
        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        checkArgs(!isNullOrEmpty(getMessage()), "missing msg arg");

        String srcName = cs.getUserName();
        String tgtName = getTargetName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such rooom");
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Src not in room");
            checkState(userIsInRoom(mgr, tgtName, roomNameLower), "Tgt not in room");
            ClientSession tgtSession = mgr.getClientSession(tgtName);
            mgr.dispatchMessage(this, tgtSession);
            return ResultMessage.success();
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
    }

}
