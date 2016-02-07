/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.servlet.api;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.List;
import webchat.core.RoomBean;
/**
 *
 * @author Edward
 */
public class RoomBeanConverter implements Converter {
    @Override
    public boolean canConvert(Class type) {
        return type.equals(RoomBean.class);
    }
    
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        RoomBean room = (RoomBean) o;
        writer.startNode("name");
        writer.setValue(room.getName());
        writer.endNode();
        if (room.isPrivate()) {
            writer.startNode("password");
            writer.setValue(room.getPassword());
            writer.endNode();
        }
        writer.startNode("owner");
        writer.setValue(room.getOwner());
        writer.endNode();
        writer.startNode("users");
        for (String name : room.listUsers()) {
            writer.startNode("user");
            writer.setValue(name);
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("tokens");
        for (String name : room.getTokenHolders()) {
            writer.startNode("token");
            writer.setValue(name);
            writer.endNode();
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        String name = null;
        String owner = null;
        String password = null;
        List<String> users = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            switch (reader.getNodeName()) {
                case "name": 
                    name = reader.getValue();
                    break;
                case "owner":
                    owner = reader.getValue();
                    break;
                case "password":
                    password = reader.getValue();
                    break;
                case "users":
                    while(reader.hasMoreChildren()) {
                        reader.moveDown();
                        users.add(reader.getValue());
                        reader.moveUp();
                    }
                    break;
                case "tokens":
                    while(reader.hasMoreChildren()) {
                        reader.moveDown();
                        tokens.add(reader.getValue());
                        reader.moveUp();
                    }
                    break;
                default:
                    assert false;
            }
            reader.moveUp();
        }
        RoomBean room = null;
        if (password != null) {
            room = new RoomBean(owner, name, password);
        } else {
            room = new RoomBean(owner, name);
        }
        for (String user : users) {
            room.addUser(user);
        }
        for (String token : tokens) {
            room.giveToken(token);
        }
        return room;
    }


    
}
