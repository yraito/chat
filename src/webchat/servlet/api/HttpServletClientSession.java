package webchat.servlet.api;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.core.ClientSession;
import webchat.core.Message;
import webchat.dao.dto.UserRecord;

/**
 * An HTTP implementation of a ClientSession that is a wrapper for an
 * HttpSession.
 *
 * @author Nick
 *
 */
public class HttpServletClientSession implements ClientSession {

    private final static Logger logger = LoggerFactory.getLogger(HttpServletClientSession.class);

    private final static String MESSAGE_QUEUE_KEY = "command message queue";

    private final static String USER_RECORD_KEY = "user record key";

    public static UserRecord getUserRecord(HttpSession hs) {
        if (hs == null) {
            return null;
        }
        return (UserRecord) hs.getAttribute(USER_RECORD_KEY);
    }
    
    public static BlockingQueue<Message> getMessageQueue(HttpSession hs) {
        return (BlockingQueue<Message>) hs.getAttribute(MESSAGE_QUEUE_KEY);
    }
    
    private HttpSession httpSession;

    private boolean isClosed;

    public HttpServletClientSession(HttpSession httpSession) {
        super();
        this.httpSession = httpSession;
        httpSession.setAttribute(MESSAGE_QUEUE_KEY, new LinkedBlockingQueue<>(1024));
    }

    /*
	 * Delegate storing session variables to underlying HttpSession. That way, on
	 * each request we can just wrap the HttpSession with a new HttpServletClientSession 
     */
    @Override
    public Integer getUserId() {
        UserRecord ur = (UserRecord) httpSession.getAttribute(USER_RECORD_KEY);
        if (ur != null) {
            return ur.getId();
        }
        return null;
    }

    @Override
    public String getUserName() {
        UserRecord ur = (UserRecord) httpSession.getAttribute(USER_RECORD_KEY);
        if (ur != null) {
            return ur.getUsername();
        }
        return null;
    }

    @Override
    public void setUser(UserRecord ur) {
        httpSession.setAttribute(USER_RECORD_KEY, ur);
    }

    @Override
    public Object attach(String key, Object value) {
        Object o = httpSession.getAttribute(key);
        httpSession.setAttribute(key, value);
        return o;
    }

    @Override
    public Object getAttachment(String key) {
        return httpSession.getAttribute(key);
    }

    @Override
    public Object detach(String key) {
        Object o = httpSession.getAttribute(key);
        httpSession.removeAttribute(key);
        return o;
    }

    /*
	 * We "write" a message to the underlying HttpSession by associating a Message
	 * queue with the HttpSession and inserting the message into the queue, which the 
	 * client can retrieve via the stream servlet.
	 * (non-Javadoc)
	 * @see core.ClientSession#writeMessage(core.Message)
     */
    @Override
    public void writeMessage(Message message) throws IOException {

        logger.debug("writeMessage to: {}, msg: {}", getUserName(), message);
        Object o = httpSession.getAttribute(MESSAGE_QUEUE_KEY);
        BlockingQueue<Message> msgQueue = null;
        if (o == null) {
            /*
			logger.debug("No queue for {}, creating new one", getUserName());
			msgQueue = new ArrayBlockingQueue<Message>(16384);
			httpSession.setAttribute(MESSAGE_QUEUE_KEY, msgQueue); */
            assert false;
        } else {
            msgQueue = (BlockingQueue<Message>) o;
        }
        if (!msgQueue.offer(message)) {
            //If the user's message queue fills up (because he has not retrieved
            //his messages for a long time, expire his session and throw an exception
            //to indicate the write failed
            closeSession();
            throw new IOException("Queue full. Closed session");
        }
    }

    @Override
    public boolean isClosed() {
        if (isClosed) {
            return true;
        }
        //A hack... to test session state, we invoke a method that will throw an exception if the
        //session has been invalidated

        try {
            httpSession.getAttribute(MESSAGE_QUEUE_KEY);
            return false;
        } catch (IllegalStateException e) {
            return true;
        }

    }

    /*
	 * We "close" the client's "connection" by invalidating their HttpSession.
	 
	 * (non-Javadoc)
	 * @see core.ClientSession#closeSession()
     */
    @Override
    public void closeSession() {
        logger.debug("Closing ClientSession for {}", getUserName());
        try {
            httpSession.invalidate();
        } catch (IllegalStateException e) {
            logger.debug("HttpSession already invalidated: {}", e.getMessage());
        }

        isClosed = true;
    }

}
