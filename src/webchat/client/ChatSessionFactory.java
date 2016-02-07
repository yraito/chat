package webchat.client;

import java.util.concurrent.Future;
import java.io.IOException;

public interface ChatSessionFactory {

	Future<ChatSession> open( String userName, String password, ChatHandler ioh) throws IOException;
}
