package webchat.client;

import java.io.IOException;

public interface ChannelFactory {

	ChatFuture<MessageChannel> open(ChatHandler ioh) throws IOException;
}
