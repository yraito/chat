package webchat.core.command;

import static webchat.core.Checks.*;
import static webchat.util.StringUtils.*;

import java.util.List;
import java.util.regex.Pattern;
import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomBean;
import webchat.core.RoomSnapshot;

/**
 * Create a new public or private room. <br />
 *
 * A private room is indicated by appending the password to the command as an
 * extra arg
 *
 * @author Nick
 *
 */
public class CreateCommand extends CommandMessage {

    private final static Pattern NAME_PATTERN  = Pattern.compile("[a-zA-Z[0-9]]{4,30}");
    
    public CreateCommand(String roomName, String password) {
        super("create", null, roomName, null, password);
    }

    public CreateCommand(String roomName) {
        super("create", null, roomName, null);
    }

    public CreateCommand() {
        
        
    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {

        checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
        checkArgs(NAME_PATTERN.matcher(getRoomName()).matches(), "room name must be alphanumeric, 4-30 chars");
        String srcName = cs.getUserName();
        String roomName = getRoomName();
        String roomLockName = roomName.toLowerCase();
        try {

            //Obtain a lock on the room name to prevent a race condition
            mgr.getLockManager().acquireLock(roomLockName);

            //Ensure there isn't already a room with this name
            checkState(!roomExists(mgr, roomName), "Room already exists");
            //checkState(hasLengthBetween(roomName, 4, 24), "Room name length");
            
            RoomBean r = null;
            //Request specifies a password. Make a private room
            if (getOtherArgs().size() == 1) {
                String roomPass = getArg(0);
                r = new RoomBean(srcName, roomName, roomPass);
                mgr.addRoom(r);

                //No password, public room
            } else {
                r = new RoomBean(srcName, roomName);
                mgr.addRoom(r);
            }
            RoomSnapshot rs = new RoomSnapshot(mgr, r);
            setAttachment(rs);
            mgr.dispatchMessage(this, cs);
            //Return success message
            return ResultMessage.success();
        } finally {
            //Should always be in finally clause so lock is released even
            //if exception was thrown
            mgr.getLockManager().releaseLock(roomLockName);
        }
    }

}
