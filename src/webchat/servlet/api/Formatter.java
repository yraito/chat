package webchat.servlet.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import webchat.core.Message;

public interface Formatter {

    void writeMessage(Message m, OutputStream os) throws IOException;

    Message readMessage(InputStream is) throws IOException;
    
    ObjectOutputStream createWriter(OutputStream os) throws IOException;
    
    ObjectInputStream createReader(InputStream is) throws IOException;

}
