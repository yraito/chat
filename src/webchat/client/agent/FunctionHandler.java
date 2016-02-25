package webchat.client.agent;

import webchat.client.blocking.BlockingRoom;

public interface FunctionHandler {

	String getName();
	
	String getDescription();
	
	MessageMatcher getMatcher();
	
	//String[] getParameterNames();
	
	//Class<?>[] getParameterTypes();
	
	//Class<?> getReturnType();
	
	Object invoke(BlockingRoom bcc, String[] params) throws Exception;
}
