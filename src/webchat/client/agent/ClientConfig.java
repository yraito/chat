package webchat.client.agent;

public class ClientConfig {

	String userName;
	String userPass;
	String host = "localhost";
	String webAppPath = "/webchat";
	String loginRelPath = "/login";
	String logoutRelPath = "/logout";
	String streamRelPath = "/stream";
	String commandRelPath = "/command";		
	int port = 8080;
	long connectTimeoutMs = 10000;
	long respTimeoutMs = 10000;
	
	String rootUrl() {
		return "http://" + host + ":" + port + webAppPath;
	}
	
	String loginUrl() {
		return rootUrl() + loginRelPath;
	}
	
	String logoutUrl() {
		return rootUrl() + logoutRelPath;
	}
	
	String streamUrl() {
		return rootUrl() + streamRelPath;
	}
	
	String commandUrl() {
		return rootUrl() + commandRelPath;
	}
}
