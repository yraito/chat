package webchat.core.command;

import static webchat.core.Checks.*;

import java.util.Collection;
import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.RoomInfo;

/**
 * Get a list of all available rooms
 *
 * @author Nick
 */
public class ListRoomsCommand extends CommandMessage {

    public ListRoomsCommand() {
        super("listrooms", null, null, null);
    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {

        Collection<RoomInfo> rs = mgr.listRoomInfos();
        return ResultMessage.success(rs);
    }

    //delegate persist to strategy? a. NullPersister or b.Config.persist
}
