package webchat.core;

import java.util.Collections;
import java.util.List;

public class MessageListMessage implements Message {

	List<Message> messages;

	public MessageListMessage(List<Message> messages) {
		super();
		this.messages = messages;
	}

        public MessageListMessage() {
            messages = Collections.emptyList();
        }
        
	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	
}
