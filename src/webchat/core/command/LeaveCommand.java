package webchat.core.command;

import static webchat.core.Checks.*;

import java.util.List;
import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomBean;
import static webchat.util.StringUtils.isNullOrEmpty;

/**
 * Leave a room
 *
 * @author Nick
 */
public class LeaveCommand extends CommandMessage {

    public LeaveCommand(String room) {
        super("leave", null, room, null);
    }

    public LeaveCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        String srcName = cs.getUserName();
        String roomNameLower = getRoomName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(roomNameLower);
            checkState(roomExists(mgr, roomNameLower), "No such room");
            //Should return success if not in room?
            checkState(userIsInRoom(mgr, srcName, roomNameLower), "Not in room");
            RoomBean rm = mgr.getRoom(roomNameLower);
            //If user is owner, randomly select new owner from user list
            //What if no users left after leave? Destroy room? If not, who is owner?
            String origOwner = rm.getOwner();
            rm.removeUser(srcName);
            System.out.println("Source: " + srcName + "; Owner: " + rm.getOwner());
            if (rm.listUsers().size() > 0 && srcName.equalsIgnoreCase(origOwner)) {
                String newOwnr = rm.chooseRandomUser();
                rm.setOwner(newOwnr);
                clearArgs();
                addArg(newOwnr);
                System.out.println("New owner: " + newOwnr);
                //security problem, need to clear args
            }
            mgr.dispatchMessage(this, cs);
            mgr.dispatchMessage(this, roomNameLower);

            if (!rm.isLobby() && rm.listUsers().isEmpty()) {
                mgr.removeRoom(rm);
            }
            return ResultMessage.success();
        } finally {
            mgr.getLockManager().releaseLock(roomNameLower);
        }
    }

}
