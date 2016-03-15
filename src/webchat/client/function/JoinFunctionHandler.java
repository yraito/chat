package webchat.client.function;

import webchat.client.agent.FunctionHandler;
import webchat.client.agent.MessageMatcher;
import webchat.client.agent.MessagePattern;
import webchat.client.blocking.BlockingRoom;
import webchat.client.blocking.BlockingSession;

public class JoinFunctionHandler implements FunctionHandler {

	MessageMatcher matcher;
	

	public JoinFunctionHandler() {
		MessagePattern mp = new MessagePattern();
		mp.concat("join ").concatArg(0, 1, 24);
		matcher = mp.buildMatcher();
	}
	
	@Override
	public String getName() {
		return "Join";
	}

	@Override
	public String getDescription() {
		return "Make me join an existing room";
	}

	@Override
	public MessageMatcher getMatcher() {
		return matcher;
	}


	@Override
	public Object invoke(BlockingRoom bcc, String[] params) throws Exception {
		bcc.sendMessage("Attempting to join " + params[0]);
		BlockingSession sess = bcc.getSession();
		if (params.length == 1) {
			sess.joinRoom(params[0]);
		} else {
			sess.joinRoom(params[0], params[1]);
		}
		return null;
	}


}
