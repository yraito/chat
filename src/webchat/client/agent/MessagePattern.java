package webchat.client.agent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static webchat.util.StringUtils.*;

public class MessagePattern {

	private static String unionof( String...strings) {
		String regex = "";
		for (String s : strings) {
			if (!isNullOrEmpty(regex)) {
				regex += '|';
			}
 			regex += '(' + s + ')';
		}
		return regex;
	}
	
	private static boolean isEnclosed(String s) {
		//return s.startsWith("(") && s.endsWith(")");
		int balance = 0;
		int index = 0;
		while (index < s.length()) {
			if (s.charAt(index) == '(') {
				balance++;
			} else if (s.charAt(index) == ')') {
				balance--;
			}
			if (balance == 0 && index < s.length() - 1) {
				return false;
			}
			index++;
		}
		return s.endsWith(")");
	}
	
	
	
	private String totalRegex = "";
	private int groups;
	private boolean prevConcat;
	private Map<Integer, Integer>  groupToArgIndex = new TreeMap<>();
	
	public MessagePattern(String...strings) {
		if (strings.length > 0) {
			concat(strings);
		}
	}
	
	public MessagePattern concat(String...strings ) {
		if (strings.length == 0) {
			return this;
		}
		
		String a = totalRegex;
		if (!isNullOrEmpty(a) && !isEnclosed(a) && !prevConcat) {
			a = '(' + a + ')';
			groups++;
		}
		String b = unionof(strings);
		if (!isNullOrEmpty(b) && !isNullOrEmpty(a) && !isEnclosed(b)) {
			b = '(' + b + ')';
			groups++;
		}
		groups+= strings.length;
		if (!isNullOrEmpty(a)) {
			//a += ' ';
			prevConcat = true;
		}
		totalRegex = a + b;
		return this;
	}

	
	public MessagePattern concat(MessagePattern that) {
		concat(that.totalRegex);
		groups += that.groups;
		for (Entry<Integer, Integer> kv : that.groupToArgIndex.entrySet()) {
			groupToArgIndex.put(kv.getKey() + groups , kv.getValue());
		}
		return this;
	}
	
	/*public MessagePattern union(MessagePattern that) {
		
	}*/

	public MessagePattern concatArg(int index, int minLen, int maxLen) {
		String s = "[\\w[\\s]]{" + minLen + "," + maxLen + "}";
		return concatArg(index, s);
	}
	
	public MessagePattern concatArg(int index, String regex) {
		if (prevConcat) {
			//totalRegex += ' ';
		}
		totalRegex += '(' + regex + ')';
		groupToArgIndex.put(++groups, index);
		return this;
	}
	
	public MessageMatcher buildMatcher() {
		Pattern pattern = Pattern.compile(totalRegex);
		List<Entry<Integer, Integer>> groupNumEntries = new LinkedList<>();
		groupNumEntries.addAll(groupToArgIndex.entrySet());
		Collections.sort(groupNumEntries, (a,b) -> a.getValue() - b.getValue());
		List<Integer> groupNums = groupNumEntries.stream().map((e)->e.getKey()).collect(Collectors.toList());
		System.out.println(groupNumEntries);
		return new MessageMatcher(pattern, groupNums);
	}
	
	public static void main(String[] args) {
		MessagePattern mp = new MessagePattern();
		mp.concat("What's ", "What is ", "How's ", "How is ")
			.concat("the ").concat("temperature ", "weather ")
			.concat("in ").concatArg(0, 1, 100)
			.concat("\\?*");
		
		System.out.println(mp.totalRegex);
		System.out.println(mp.groups);
		System.out.println(mp.groupToArgIndex);
		MessageMatcher mm = mp.buildMatcher();
		List l0 = mm.match("om hgfh djdj y weather what");
		List l1 = mm.match("How's the weather in 2323?");
		System.out.println(l0);
		System.out.println(l1);
	}
}
