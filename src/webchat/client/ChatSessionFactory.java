package webchat.client;

import java.io.IOException;

public interface ChatSessionFactory {

	ChatFuture<ChatSession> open( String userName, String password, ChatHandler ioh) throws IOException;
}
