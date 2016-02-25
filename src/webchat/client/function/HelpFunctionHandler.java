package webchat.client.function;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import webchat.client.agent.FunctionHandler;
import webchat.client.agent.FunctionProcessor;
import webchat.client.agent.MessageMatcher;
import webchat.client.agent.MessagePattern;
import webchat.client.blocking.BlockingRoom;

public class HelpFunctionHandler implements FunctionHandler {

	Collection<FunctionHandler> handlers;
	MessageMatcher matcher;
	
	public HelpFunctionHandler(FunctionProcessor fp) {
		super();
		this.handlers = fp.getHandlers();
		MessagePattern mp = new MessagePattern();
		mp.concat("Help", "Get commands", "List commands", "Commands");
		matcher = mp.buildMatcher();
	}
	
	@Override
	public String getName() {
		return "Help";
	}

	@Override
	public String getDescription() {
		return "Get a list of available commands";
	}


	@Override
	public MessageMatcher getMatcher() {
		return matcher;
	}
	
	@Override
	public Object invoke(BlockingRoom bcc, String[] params) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (FunctionHandler handler : handlers) {
			String funcName = handler.getName();
			String descr = handler.getDescription();
			sb.append(funcName).append(": ").append(descr).append("<br />");
		}
		return sb.toString();
	}


}
