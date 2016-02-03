package webchat.core;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.dao.DaoConnection;
import webchat.dao.DaoException;
import webchat.dao.EventDao;
import webchat.dao.dto.EventRecord;
import webchat.dao.dto.UserRecord;

/** 
 * A message sent from a client to the server representing a command, and resenr from
 * the server to clients as a notification of execution of the command. <br />
 * 
 * <p> Each of the derived classes corresponds to a command the chatserver
 * understands (e.g. "join room" or "kick user"). The general sequence of steps
 * for each method is: </p>
 * 
 * <ol>
 * <li>Check who sender is. This information is available as a "session variable", 
 * ie it is attached to each ClientSession object. Note this is different from the 
 * CommandMessage.source field, which is used by the server when forwarding to indicate
 * to other clients the true origin of the message</li>
 * 
 * <li> Perform a series of pre-condition checks, throwing an exception if any fail.
 * The exception will be reported back to the sender as an error response. </li>
 * 		<ol>
 * 		<li> check format of arguments. E.g. for a "message" command, ensure
 * 			there are two String arguments of acceptable length </li>
 * 		<li> check for valid state. E.g. for a "create room" command, ensure
 * 			the room doesn't already exist </li>
 * 		<li> check permissions. E.g. for a "kick" command, ensure that the user
 * 			is the room owner or a token holder </li>
 * 		</ol>
 * 
 * <li> Update state if necessary. E.g. add user to data structure after a "join room" </li>
 * 
 * <li> Create a record of the event in the database </li>
 * 
 * <li> Dispatch notifications to other users if necessary. The server simply forwards
 * the incoming CommandMessage to the appropriate users after filling out the CommandMessage.source
 * field </li>
 * </ol>
 * @author Nick
 */
public abstract class CommandMessage implements Message{
	
	private final static Logger logger = LoggerFactory.getLogger(CommandMessage.class);
	
	protected final static int MAXNAMELEN = 24;
	
	protected final static int MAXPASSLEN = 24;
	
	protected final static int MAXMSGLEN = 300;
	
	protected String command;
	protected String sourceName;
	protected String targetName;
	protected String roomName;
	protected String message;
	protected List<String> otherArgs = new LinkedList<>();
	protected long timeStamp;

	protected CommandMessage(String command, String tgt, String rm, String msg, String...arg) {
		this.command = command;
		this.targetName = tgt;
		this.roomName = rm;
		this.message = msg;
		this.otherArgs.addAll(Arrays.asList(arg));
	}
	
	public CommandMessage() {
		
		
	}
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getSourceName() {
		return sourceName;
	}


	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}


	public String getTargetName() {
		return targetName;
	}


	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}


	public String getRoomName() {
		return roomName;
	}


	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public List<String> getOtherArgs() {
		return otherArgs;
	}


	public void setOtherArgs(List<String> otherArgs) {
		this.otherArgs.clear();
		this.otherArgs.addAll(otherArgs);
	}

	public void clearOtherArgs() {
		otherArgs.clear();
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getArg(int index) {
		if (index >= otherArgs.size()) {
			return null;
		}
		return otherArgs.get(index);
	}
	public void addArg( String arg) {
		otherArgs.add( arg);
	}
	
	public void clearArgs() {
		otherArgs.clear();
	}

	@Override
	public String toString() {
		String s = command 
				+ " <" + sourceName + "> "
				+ " <" + targetName + "> "
				+ " <" + roomName + "> "
				+ " <" + message + "> "
				+ " <" + otherArgs + "> ";
		return s;
	}
	
	public void persist(int senderId, DaoConnection dc, ChatManager mgr) throws DaoException{
	
		logger.debug("Preparing to save CommandMessage from id={} as EventRecord", senderId);
		EventRecord ge = new EventRecord(command, senderId);
		
		if (targetName != null) {
			UserRecord tgtRecord = dc.getUserDao().find(targetName);
			if (tgtRecord != null) {
				ge.setTargetId(tgtRecord.getId());
			}
		}
		ge.setRoomName(roomName);
		ge.setMessage(message);
		ge.setTimestamp(System.currentTimeMillis());
		ge.setSourceId(senderId);
		EventDao ed = dc.getEventDao();
		ed.save(ge);
		logger.debug("Saved EventRecord: {} via EventDao: {}", ge, ed);
	}
	

	
	public abstract ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException;


	//protected abstract boolean isAuth();
	
}
