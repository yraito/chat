package webchat.core.command;

import static webchat.core.Checks.*;
import static webchat.util.StringUtils.*;

import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;

/**
 * Send a message to a room
 * @author Nick
 */
public class MessageCommand extends CommandMessage {

	public MessageCommand(String rm, String msg) {
		super("message", null, rm, msg);
	}
	
	public MessageCommand() {
		
	}

	@Override
	public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
		
		checkArgs(!isNullOrEmpty(getRoomName()), "missing room arg");
		checkArgs(!isNullOrEmpty(getMessage()), "missing msg arg");
		String srcName = (String) cs.getUserName();
		String roomNameLower = getRoomName().toLowerCase();
		String message = getMessage();
		try {
			mgr.getLockManager().acquireLock(roomNameLower);
			checkState(roomExists(mgr, roomNameLower), "No such rooom");
			checkState(userIsInRoom(mgr, srcName, roomNameLower), "Src not in room");
			this.setMessage(safeMessage(message));
			mgr.dispatchMessage(this, roomNameLower);
			return ResultMessage.success();
		} finally {
			mgr.getLockManager().releaseLock(roomNameLower);
		}	
	}
}
