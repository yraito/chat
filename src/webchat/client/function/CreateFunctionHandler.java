package webchat.client.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import webchat.client.agent.FunctionHandler;
import webchat.client.agent.MessageMatcher;
import webchat.client.agent.MessagePattern;
import webchat.client.blocking.BlockingRoom;

public class CreateFunctionHandler implements FunctionHandler {

	MessageMatcher matcher;
	

	public CreateFunctionHandler() {
		MessagePattern mp = new MessagePattern();
		mp.concat("make a " , "create a ", "open a ")
				.concat("room ", "new room")
				.concat("called ", "named ")
				.concatArg(0, "[\\w]{1,24}");
		matcher = mp.buildMatcher();
	}
	

	
	@Override
	public String getName() {
		return "CreateRoom";
	}

	@Override
	public String getDescription() {
		return "Make me create a new room";
	}

	@Override
	public MessageMatcher getMatcher() {
		return matcher;
	}
	
	@Override
	public Object invoke(BlockingRoom bcc, String[] params) throws Exception {
		bcc.sendMessage("Attempting to create room " + params[0]);
		if (params.length == 1) {
			bcc.getSession().createRoom(params[0]);
		} else {
			bcc.getSession().createRoom(params[0], params[1]);
		}
		return "Room created";
	}

	public static void main(String[] args) throws IOException {
		MessagePattern mp = new MessagePattern();
		mp.concat("make a " , "create a ", "open a ")
				.concat("room ", "new room ")
				.concat("called ", "named ")
				.concatArg(0, "[\\w]{1,24}");
		MessageMatcher matcher = mp.buildMatcher();
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
		String s;
		while ( !(s = rdr.readLine()).equalsIgnoreCase("quit") ) {
			System.out.println(matcher.match(s));
		}
	}

}
