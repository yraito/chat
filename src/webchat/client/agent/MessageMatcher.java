package webchat.client.agent;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageMatcher {

	Pattern pattern;
	List<Integer> groupNums;
	
	public MessageMatcher(Pattern pattern, List<Integer> groupNums) {
		super();
		this.pattern = pattern;
		this.groupNums = groupNums;
	}
	
	public List<String> match(String message) {
		Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {
			List<String> args = new LinkedList<>();
			for (int j = 0; j < groupNums.size(); j++) {
				String arg = matcher.group(groupNums.get(j));
				System.out.println(arg);
				args.add(arg);
			}
			return args;
		}
		return null;
	}


}
