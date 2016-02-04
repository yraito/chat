package webchat.servlet.api;

import webchat.core.command.MessageCommand;
import webchat.core.command.KickCommand;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import webchat.core.Message;
import webchat.core.MessageListMessage;
import webchat.core.ResultMessage;



public class XStreamFormatter implements Formatter {


	XStream xstream;
	
	public XStreamFormatter() {
		xstream = new XStream(new StaxDriver());
		xstream.alias("Result", ResultMessage.class);
		xstream.alias("Messages", MessageListMessage.class);
		xstream.addImplicitCollection(MessageListMessage.class, "messages");

	}
	
	@Override
	public void writeMessage(Message m, OutputStream os) throws IOException {
		xstream.toXML(m, os);
	}

	@Override
	public Message readMessage(InputStream is) throws IOException {
		Object o = xstream.fromXML(is);
		if ( !(o instanceof Message) ) {
			throw new IOException("Expecting a Message, got a " + o.getClass());
		}
		return (Message) o;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Message m0 = new MessageCommand("someroom", "yadayada");
		Message m1 = new KickCommand("someuser", "someroom", "because");
		Formatter f = new XStreamFormatter();

	}

}
