package webchat.client.blocking;

import java.io.IOException;

import java.util.concurrent.Future;

import webchat.client.ChatSession;
import webchat.client.ChatSessionFactory;

public class BlockingConnector {

	private ChatSessionFactory iocfact;
	
	public BlockingConnector(ChatSessionFactory iocfact) {
		this.iocfact = iocfact;
	}
	
	public BlockingSession connect(String userName, String password, long timeoutMs) throws IOException{
		EventManagerAdapter ema = new EventManagerAdapter();
		Future<ChatSession> iocfut = iocfact.open(userName, password, ema);
		ChatSession ioc = null;
		try {
			ioc = BlockingSession.waitFor(iocfut, timeoutMs);
			return new BlockingSession(userName, password, ioc, ema);
		} catch (IOException e) {
			iocfut.cancel(true);
			throw e;
		} 
	}
        
       
}
