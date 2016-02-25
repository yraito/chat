package webchat.client.blocking;

import java.io.IOException;

import java.util.concurrent.Future;
import webchat.client.ChatFuture;
import webchat.client.ChatHandler;

import webchat.client.ChatSession;
import webchat.client.ChatSessionFactory;
import webchat.core.CommandMessage;
import webchat.core.Message;

public class BlockingConnector {

	private ChatSessionFactory iocfact;
	
	public BlockingConnector(ChatSessionFactory iocfact) {
		this.iocfact = iocfact;
	}
	
	public BlockingSession connect(String userName, String password) throws IOException{
		EventManager eventMgr = new EventManager();
                BlockingSession sess = new BlockingSession(userName, password, null, eventMgr);
                ChatHandler chatHandler = new ChatHandler() {
                    @Override
                    public void onMessageReceived(ChatSession chatSess, Message m) {
                        if (m instanceof CommandMessage) {
                            CommandMessage cmdMsg = (CommandMessage) m;
                            //BlockingSession sess = (BlockingSession) chatSess;
                            eventMgr.dispatch(new MessageEvent(sess,  cmdMsg));
                        }
                    }
                };
		ChatFuture<ChatSession> iocfut = iocfact.open(userName, password, chatHandler);
		ChatSession chatSess = iocfut.getUninterruptibly();
                sess.wrappedSess = chatSess;
                return sess;
	}
        
       
}
