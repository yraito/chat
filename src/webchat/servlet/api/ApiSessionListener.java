package webchat.servlet.api;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.core.ChatManager;

import static webchat.servlet.api.ApiServletListener.*;

public class ApiSessionListener implements HttpSessionListener {

    private final static Logger logger = LoggerFactory.getLogger(ApiSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.debug("Http session created");
        se.getSession().setMaxInactiveInterval(240);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession httpSess = se.getSession();
        ChatManager chatMgr = (ChatManager) httpSess.getServletContext().getAttribute(CHAT_MANAGER_KEY);
        if (chatMgr == null) {
            logger.error("No ChatManager attached to ServletContext");
        } else if (HttpServletClientSession.getUserRecord(httpSess) != null){
            HttpServletClientSession clientSess = new HttpServletClientSession(httpSess);
            logger.debug("Destroying session for {}", clientSess.getUserName());
            chatMgr.onEndSession(clientSess);
        } else {
            logger.debug("Destroying session, not logged in");
        }
    }

}
