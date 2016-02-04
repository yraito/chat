package webchat.servlet.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.core.Message;
import webchat.core.MessageListMessage;
import webchat.dao.dto.UserRecord;


/**
 * Servlet for a client to fetch his messages/notifications. <br />
 *
 * The client simply sends a content-less GET/POST to the servlet (with the
 * session id specified as a cookie or in a URL query.
 *
 * @author Nick
 *
 */
public class ApiStreamServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(ApiStreamServlet.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ResponseStrategy strategy = new LongPollingResponseStrategy();

    @Override
    public void init() throws ServletException {
        logger.info("Init ApiStreamServlet");
    }

    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        logger.debug("doGet");
        try {
            doGetOrPost(req, resp);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        logger.debug("doPost");
        try {
            doGetOrPost(req, resp);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSess = req.getSession(false);
        UserRecord usrRecord = HttpServletClientSession.getUserRecord(httpSess);
        if (usrRecord == null) {
            logger.debug("No UserRecord attached to session, responding with 401 unauthorized");
            ApiResponder.get(req, resp).respondUnauthorized("Not logged in");
        } else {
            //Fetch user's messages and write them to the response's output stream
            BlockingQueue<Message> msgQueue = HttpServletClientSession.getMessageQueue(httpSess);
            String username = usrRecord.getUsername();
            logger.debug("Fetched queue for {} and responding with {}", username, strategy.getClass().getName());
            strategy.respondMessages(msgQueue, req, resp);
            resp.getOutputStream().close();

        }
    }

    interface ResponseStrategy {

        void respondMessages(BlockingQueue<Message> msgQueue, HttpServletRequest req, HttpServletResponse resp)
                throws IOException;
    }

    /**
     *
     * Immediately respond with whatever messages are available, then terminate response
     *
     * @author Nick
     *
     */
    static class PollingResponseStrategy implements ResponseStrategy {

        @Override
        public void respondMessages(BlockingQueue<Message> msgQueue, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Message m = null;
            ArrayList<Message> msgLst = new ArrayList<>();
            while ((m = msgQueue.poll()) != null) {
                msgLst.add(m);
                logger.debug("Fetched message: {}", m);
            }
            MessageListMessage mlm = new MessageListMessage(msgLst);
            ApiResponder.get(req, resp).respondOK(mlm);
        }
    }

    /**
     * Block until some messages are available or max timeout, then respond and terminate response
     *
     * @author Nick
     *
     */
    static class LongPollingResponseStrategy implements ResponseStrategy {

        @Override
        public void respondMessages(BlockingQueue<Message> msgQueue, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

            Message m = null;
            MessageListMessage mlm = null;
          
            try {
                m = msgQueue.poll(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //
            }
            if (m != null) {
                logger.debug("Fetched first message from queue: {}", m);
                ArrayList<Message> msgList = new ArrayList<>();
                while ((m = msgQueue.poll()) != null) {
                    logger.debug("Fetched another message from queue: {}", m);
                    msgList.add(m);
                }
                mlm = new MessageListMessage(msgList);
            } else {
                logger.debug("No messages in queue");
                mlm = new MessageListMessage();
            }
            ApiResponder.get(req, resp).respondOK(mlm);

        }
    }

    /**
     * Keep response open as long as possible and push messages to client.
     * Periodically send heartbeat messages to help keep the connection open
     *
     * TODO Implement
     *
     * @author Nick
     *
     */
    static class StreamingResponseStrategy implements ResponseStrategy {

        @Override
        public void respondMessages(BlockingQueue<Message> msgQueue, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

}
