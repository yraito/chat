/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.servlet.api;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.List;
import java.util.ArrayList;
import webchat.core.*;
/**
 *
 * @author Edward
 */
public class CommandMessageConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return CommandMessage.class.isAssignableFrom(type);
    }
    
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        CommandMessage cmd = (CommandMessage) o;
        writer.addAttribute("command", cmd.getCommand());
        writer.startNode("timestamp");
        writer.setValue(Long.toString(cmd.getTimeStamp()));
        writer.endNode();
        if (cmd.getSourceName() != null) {
            writer.startNode("source");
            writer.setValue(cmd.getSourceName());
            writer.endNode();
        }
        if (cmd.getTargetName() != null) {
            writer.startNode("target");
            writer.setValue(cmd.getTargetName());
            writer.endNode();
        }
        if (cmd.getRoomName() != null) {
            writer.startNode("room");
            writer.setValue(cmd.getRoomName());
            writer.endNode();
        } 
        if (cmd.getMessage() != null) {
            writer.startNode("message");
            writer.setValue(cmd.getMessage());
            writer.endNode();
        }
        List<String> args = cmd.getOtherArgs();
        if (args != null && !args.isEmpty()) {
            writer.startNode("args");
            for (String arg : args) {
                writer.startNode("arg");
                writer.setValue(arg);
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        String command = reader.getAttribute("command");
        String source = null;
        String target = null;
        String room = null;
        String message = null;
        String timestamp = null;
        ArrayList<String> args = null;
    
 
        while(reader.hasMoreChildren()) {
            
            reader.moveDown();
            String nodeName = reader.getNodeName();
            String nodeValue = reader.getValue();
            switch(nodeName) {
                case "source": 
                    source = nodeValue;
                    break;
                case "target":
                    target = nodeValue;
                    break;
                case "room":
                    room = nodeValue;
                    break;
                case "message":
                    message = nodeValue;
                    break;
                case "timestamp":
                    timestamp = nodeValue;
                    break;
                case "args":
                    args = new ArrayList<>();
                    while(reader.hasMoreChildren()) {
                        reader.moveDown();
                        args.add(reader.getValue());
                        reader.moveUp();
                    }
                    break;
            }
            reader.moveUp();
        }
        
        CommandMessage cm = CommandMessages.newCommandMessage(command, target, room, message);
        if (cm == null) {
            throw new ConversionException("Can't unmarshal CommandMessage");
        }
        cm.setSourceName(source);
        cm.setTimeStamp(Long.valueOf(timestamp));
        if (args != null) {
            cm.setOtherArgs(args);
        }
        return cm;
    }

    
    
}
