package webchat.client.blocking;

import java.io.IOException;
import java.util.Collection;

import webchat.core.*;
import webchat.core.command.*;

/**
 * Client's interface to a room he is in. 
 * 
 * @author Nick
 *
 */
public class BlockingChannel {
	
	private RoomBean room;
	private BlockingSession sess;
	
	public BlockingChannel() {

	}
	
	public void init(RoomBean r, BlockingSession s) {
		sess = s;
		room = r;
                sess.getEventManager().addListener(r.getName(), new UpdatingListener());
	}

        public BlockingSession getSession() {
		return sess;
	}
        
        public void addListener(ChannelListener cl) {
            sess.getEventManager().addListener(getName(), cl);
        }
        
        public void removeListener(ChannelListener cl) {
            sess.getEventManager().removeListener(getName(), cl);
        }
        
	public String getName() {
		return room.getName();
	}
	
	public String getPassword() {
		return room.getPassword();
	}
	
	public String getOwner() {
		return room.getOwner();
	}
		
	public Collection<String> listUsers() {
		return room.listUsers();
	}
        
	public Collection<String> listTokenHolders() {
		return room.getTokenHolders();
	}
        
	public void grantToken(String user) throws IOException, ChatException {
		sess.writeRead( new GrantCommand(user, getName()) );
	}
	
	public void revokeToken(String user) throws IOException, ChatException {
		sess.writeRead( new RevokeCommand(user, getName()) );
	}

	public void sendMessage(String message) throws IOException, ChatException {
		sess.writeRead( new MessageCommand(getName(), message) );

	}
	
	public void sendWhisper(String user, String message) throws IOException, ChatException {
		sess.writeRead( new WhisperCommand(user, getName(), message) );
	}
	
	public void kickUser(String user, String reason) throws IOException, ChatException {
		sess.writeRead( new KickCommand(user, getName(), reason) );
	}

	public void leaveRoom() throws IOException, ChatException {
		sess.writeRead( new LeaveCommand(getName()) );
                sess.getEventManager().removeListeners(getName());
	}
	
	public void destroyRoom() throws IOException, ChatException {
		sess.writeRead( new DestroyCommand(getName()));
                sess.getEventManager().removeListeners(getName());
	}
	
	private class UpdatingListener implements ChannelListener {

		@Override
		public void onJoin(String src) {
			room.addUser(src);
		}

		@Override
		public void onLeave(String src, String newOwnr) {
			room.removeUser(src);
			if (newOwnr != null) {
				room.setOwner(newOwnr);
			}
		}

		@Override
		public void onKick(String src, String tgt, String rsn) {
			room.removeUser(tgt);
		}

		@Override
		public void onGrantToken(String tgt) {
			room.giveToken(tgt);
		}

		@Override
		public void onRevokeToken(String tgt) {
			room.takeToken(tgt);
		}

		@Override
		public void onNewOwner(String tgt) {
			room.setOwner(tgt);
		}
	}
}
