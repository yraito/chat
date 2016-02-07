package webchat.client.agent;

import webchat.client.blocking.BlockingChannel;

public interface FunctionHandler {

	String getName();
	
	String getDescription();
	
	MessageMatcher getMatcher();
	
	//String[] getParameterNames();
	
	//Class<?>[] getParameterTypes();
	
	//Class<?> getReturnType();
	
	Object invoke(BlockingChannel bcc, String[] params) throws Exception;
}
