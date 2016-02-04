package webchat.core;

import webchat.core.command.StatusCommand;
import webchat.core.command.LeaveCommand;
import webchat.core.command.CreateCommand;
import webchat.core.command.ListUsersCommand;
import webchat.core.command.MessageCommand;
import webchat.core.command.JoinCommand;
import webchat.core.command.GrantCommand;
import webchat.core.command.ListRoomsCommand;
import webchat.core.command.KickCommand;
import webchat.core.command.RevokeCommand;
import webchat.core.command.DestroyCommand;
import webchat.core.command.WhisperCommand;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.core.command.LoginCommand;
import webchat.core.command.LogoutCommand;

public class CommandMessages {
	
	private final static Logger logger = LoggerFactory.getLogger(CommandMessages.class);

	static final Map<String, Class<? extends CommandMessage>> commands = new ConcurrentHashMap<>();
	
	static {
		commands.put("create", CreateCommand.class);
		commands.put("destroy", DestroyCommand.class);
		commands.put("grant", GrantCommand.class);
		commands.put("join", JoinCommand.class);
		commands.put("kick", KickCommand.class);
		commands.put("leave", LeaveCommand.class);
                commands.put("login", LoginCommand.class);
		commands.put("listrooms", ListRoomsCommand.class);
		commands.put("listusers", ListUsersCommand.class);
		commands.put("message", MessageCommand.class);
		commands.put("revoke", RevokeCommand.class);
		commands.put("status", StatusCommand.class);
		commands.put("whisper", WhisperCommand.class);
                commands.put("logout", LogoutCommand.class);
	}
	
	/**
	 * Instantiates and returns the sub-class corresponding to the command string, or null
	 * if no corresponding class found
	 * 
	 * @param cmd the name of the command
	 * @param tgt
	 * @param rm
	 * @param msg
	 * @param args
	 * @return
	 */
	public static CommandMessage newCommandMessage(String cmd, String tgt, String rm, String msg, String...args) {
		Class<? extends CommandMessage> clazz = commands.get(cmd.trim().toLowerCase());
		if (clazz == null) {
			logger.info("No CommandMessage class found for \"{}\"", cmd);
			return null;
		}

		try {
			logger.debug("CommandMessage class found for \"{}\": {}", cmd, clazz.getName());
			CommandMessage cmdMsg = clazz.newInstance();
			cmdMsg.setCommand(cmd);
			cmdMsg.setTargetName(tgt);
			cmdMsg.setRoomName(rm);
			cmdMsg.setMessage(msg);
			for (int j = 0; args != null && j < args.length; j++) {
				cmdMsg.addArg(args[j]);
			}
			return cmdMsg;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Can't instantiate CommandMessage ", e);
		}

	}
}
