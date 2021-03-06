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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import webchat.core.*;
import webchat.core.command.JoinCommand;

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
        Object attachment = cmd.getAttachment();
        if (attachment != null) {
            System.out.println(attachment);
            writer.startNode("attachment");
            writer.addAttribute("class", attachment.getClass().getName());
            mc.convertAnother(attachment);
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
        Object attachment = null;

        while (reader.hasMoreChildren()) {

            reader.moveDown();
            String nodeName = reader.getNodeName();
            String nodeValue = reader.getValue();
            //System.out.println(reader.getAttribute("class"));
            switch (nodeName) {
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
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        args.add(reader.getValue());
                        reader.moveUp();
                    }
                    break;
                case "attachment":
                  
                    String clazzName = reader.getAttribute("class");
                    System.out.println(clazzName);
                    if (clazzName == null) {
                        System.out.println("null clazz attribute");
                        //break;
                    }
                      //reader.moveDown();
                    try {
                        //Class clazz = Class.forName(clazzName);
                        attachment = uc.convertAnother(uc.currentObject(), RoomSnapshot.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        //reader.moveUp();
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
        if (attachment != null) {
            cm.setAttachment(attachment);
        }
        return cm;
    }

    public static void main(String[] args) throws IOException {
        Formatter f = new XStreamFormatter();
        JoinCommand joinCmd = new JoinCommand("abc", "def");
        RoomSnapshot.RoomUser userA = new RoomSnapshot.RoomUser("abd", RoomSnapshot.RoomPrivs.OWNER,
                UserStatus.BUSY);
        RoomSnapshot.RoomUser userB = new RoomSnapshot.RoomUser("asbd", RoomSnapshot.RoomPrivs.OWNER,
                UserStatus.AWAY);
        ArrayList<RoomSnapshot.RoomUser> users = new ArrayList<>();
        users.add(userA);
        users.add(userB);
        RoomSnapshot rs = new RoomSnapshot("ssfs", "sfsdfs", users);
        joinCmd.setAttachment(rs);
        System.out.println(joinCmd);
        f.writeMessage(joinCmd, System.out);
        System.out.println();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        f.writeMessage(joinCmd, baos);
        Message m = f.readMessage(new ByteArrayInputStream(baos.toByteArray()));
        System.out.println(m);
    }
}
