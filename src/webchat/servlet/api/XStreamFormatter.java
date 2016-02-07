package webchat.servlet.api;

import webchat.core.command.MessageCommand;
import webchat.core.command.KickCommand;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import webchat.core.CommandMessages;
import webchat.core.Message;
import webchat.core.MessageListMessage;
import webchat.core.ResultMessage;
import webchat.core.*;

public class XStreamFormatter implements Formatter {

    XStream xstream;

    public XStreamFormatter() {
        xstream = new XStream(new StaxDriver());
        initResultMessage();
        initCommandMessage();
        xstream.alias("room", RoomBean.class);
        xstream.alias("roomInfo", RoomInfo.class);
        xstream.registerConverter(new RoomBeanConverter());

    }
    
    private void initResultMessage() {
        xstream.alias("result", ResultMessage.class);
        xstream.useAttributeFor(ResultMessage.class, "type");
        xstream.aliasField("status", ResultMessage.class, "type");
        xstream.aliasField("error", ResultMessage.class, "err");
        xstream.aliasField("content", ResultMessage.class, "res");
    }

    private void initCommandMessage() {
        xstream.alias("command", CommandMessage.class);
        for (Map.Entry<String, Class<? extends CommandMessage>> entry : 
                CommandMessages.getAllCommands().entrySet()) {
            xstream.alias("command", entry.getValue());
        }
        xstream.registerConverter(new CommandMessageConverter());
    }
    public void writeMessage(Message m, OutputStream os) throws IOException {
        xstream.toXML(m, os);
    }

    public Message readMessage(InputStream is) throws IOException {
        Object o = xstream.fromXML(is);
        if (!(o instanceof Message)) {
            throw new IOException("Expecting a Message, got a " + o.getClass());
        }
        return (Message) o;
    }

    @Override
    public ObjectOutputStream createWriter(OutputStream os) throws IOException{
        return xstream.createObjectOutputStream(os);
    }

    @Override
    public ObjectInputStream createReader(InputStream is) throws IOException {
        return xstream.createObjectInputStream(is);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Message m0 = new MessageCommand("someroom", "yadayada");
        Message m1 = new KickCommand("someuser", "someroom", "because");
        Formatter f = new XStreamFormatter();

    }

}
