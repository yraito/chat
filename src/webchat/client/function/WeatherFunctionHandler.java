package webchat.client.function;

import webchat.client.agent.FunctionHandler;
import webchat.client.agent.MessagePattern;
import webchat.client.agent.MessageMatcher;
import webchat.client.*;
import webchat.client.blocking.BlockingRoom;

public class WeatherFunctionHandler implements FunctionHandler  {

	WeatherFetcher wf = new WeatherFetcher();
	MessageMatcher matcher;
	
	public WeatherFunctionHandler() {
		MessagePattern mp = new MessagePattern();
		mp.concat("what's ", "what is ", "how's ", "how is ")
			.concat("the ").concat("temperature ", "weather ")
			.concat("in ").concatArg(0, 1, 100)
			.concat("\\?*");
		matcher = mp.buildMatcher();
	}
	
	@Override
	public String getName() {
		return "weather";
	}

	@Override
	public String getDescription() {
		return "Report current weather in a city";
	}

	@Override
	public MessageMatcher getMatcher() {
		return matcher;
	}

	@Override
	public Object invoke(BlockingRoom bcc, String[] params) throws Exception {
		return wf.fetch(params[0]);
	}


}
