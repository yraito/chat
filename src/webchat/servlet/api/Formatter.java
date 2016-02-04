package webchat.servlet.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import webchat.core.Message;

public interface Formatter {

    void writeMessage(Message m, OutputStream os) throws IOException;

    Message readMessage(InputStream is) throws IOException;

}
