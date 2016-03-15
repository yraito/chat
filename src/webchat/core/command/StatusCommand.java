package webchat.core.command;

import webchat.core.ChatException;
import webchat.core.ChatManager;

import webchat.core.UserStatus;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomBean;
import static webchat.util.StringUtils.isNullOrEmpty;
import static webchat.core.Checks.checkArgs;
import static webchat.core.Checks.checkArgs;
import static webchat.core.Checks.checkArgs;
import static webchat.core.Checks.checkArgs;

/**
 * Update user's status
 *
 * @@author Nick
 */
public class StatusCommand extends CommandMessage {

    public StatusCommand(UserStatus us) {
        super("status", null, null, null, us.toString());
    }

    public StatusCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {

        //away, busy... busy, away
        UserStatus usrStat = parseStatus(getArg(0));
        String srcName = cs.getUserName();
        String srcNameLower = srcName.toLowerCase();
        try {
            mgr.getLockManager().acquireLock(srcNameLower);
            mgr.setUserStatus(srcName, usrStat);
            mgr.dispatchMessage(this, cs);
            for (RoomBean r : mgr.getRooms()) {

                if (!r.hasUser(srcName)) {
                    continue;
                }
                String roomNameLower = r.getName().toLowerCase();
                try {
                    //create room snapshot, receive status update, receive room snapshot
                    mgr.getLockManager().acquireLock(roomNameLower);
                    mgr.dispatchMessage(this, roomNameLower, cs);
                } finally {
                    mgr.getLockManager().releaseLock(roomNameLower);
                }

            }
        } finally {
            mgr.getLockManager().releaseLock(srcNameLower);
        }

        return ResultMessage.success();
    }

    private UserStatus parseStatus(String usStr) throws ChatException {
        checkArgs(!isNullOrEmpty(usStr), "missing status arg");
        UserStatus us = UserStatus.fromString(usStr);
        checkArgs(us != null, "unrecognized status");
        return us;
    }

}
