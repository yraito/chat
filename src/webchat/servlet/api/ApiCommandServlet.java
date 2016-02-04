package webchat.servlet.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.CommandMessages;
import webchat.core.ProtocolException;
import webchat.core.ResultMessage;
import webchat.util.StringUtils;


/**
 * Accepts chat commands from the client and responds with the result. <br />
 *
 * In addition to parsing the commands from the request's body, the servlet also
 * accepts commands to be submitted via request (URL encoded) parameters. In the
 * case of GETs, the command components are specified in the query string: <br/>
 *
 * <p>
 * GET
 * .../command?command=whisper&room=myroom&user=myfriend&message=hi+how+are+you
 * </p><br/>
 *
 * For POSTing parameters, the Content-Type of the request should be set to
 * application/x-www-form-urlencoded. Otherwise the request body will be parsed
 * normally (as XML)
 *
 *
 * @author Nick
 *
 */
public class ApiCommandServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(ApiCommandServlet.class);

    private static final long serialVersionUID = 1L;

    ChatManager chatManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Init ApiCommandServlet");
        chatManager = (ChatManager) config.getServletContext().getAttribute(ApiServletListener.CHAT_MANAGER_KEY);
        if (chatManager == null) {
            throw new ServletException("No chat manager");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("doGet");
        process(req, resp, true);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("doPost");
        boolean fromParams = ("application/x-www-form-urlencoded").equalsIgnoreCase(req.getContentType());
        process(req, resp, fromParams);
    }

    /**
     *
     * @param req
     * @param resp
     * @param fromParams
     * @throws IOException
     */
    private void process(HttpServletRequest req, HttpServletResponse resp, boolean fromParams) throws IOException {
 HttpSession httpSess = req.getSession();
      /* 
        UserRecord usrRecord = HttpServletClientSession.getUserRecord(httpSess);
        if (usrRecord == null) {
            logger.debug("No UserRecord attached to session, responding unauthorized");
            ApiResponder.get(req, resp).respondUnauthorized("Not logged in");
        } else {
         */   ClientSession clientSess = new HttpServletClientSession(httpSess);
            ApiResponder responder = ApiResponder.get(req, resp);
            CommandMessage commandMsg = null;
            try {
                if (fromParams) {
                    commandMsg = buildCommandFromParams(req.getParameterMap());
                } else {
                    commandMsg = buildCommandFromInputStream(req.getInputStream());
                }
                ResultMessage resultMsg = chatManager.processMessage(clientSess, commandMsg);
                responder.respond(resultMsg);
            } catch (ProtocolException e) {
                logger.debug("Protocol exception, responding client error");
                responder.respondClientError(e.getMessage());
            }
    //}

    }

    /*
	 * Parse the CommandMessage from the request's parameters
     */
    private CommandMessage buildCommandFromParams(Map<String, String[]> paramMap) throws ProtocolException {

        String cmd = getFirstValue(paramMap, "command", true);
        String tgt = getFirstValue(paramMap, "target", false);
        String rm = getFirstValue(paramMap, "room", false);
        String msg = getFirstValue(paramMap, "message", false);
        String[] args = getValues(paramMap, "args");
        CommandMessage cmdMsg = CommandMessages.newCommandMessage(cmd, tgt, rm, msg, args);
        logger.debug("Parsed CommandMessage from request params: {}", cmdMsg);
        if (cmdMsg == null) {
            throw new ProtocolException("Unrecognized command: " + cmd);
        }
        return cmdMsg;
    }

    /*
	 * Parse the CommandMessage from the request's entity
     *TODO
     */
    private CommandMessage buildCommandFromInputStream(InputStream is) throws ProtocolException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /*
	 * 
     */
    private static String getFirstValue(Map<String, String[]> paramMap, String key, boolean required) throws ProtocolException {
        String[] vs = getValues(paramMap, key);
        String v = null;
        if (vs != null && vs.length > 0) {
            if (!StringUtils.isNullOrEmpty(vs[0])) {
                v = vs[0];
            }
        }
        if (v == null && required) {
            throw new ProtocolException("Bad request: no " + key);
        }
        return v;
    }

    /*
	 * Searches for the value of a key, case-insensitively
     */
    private static String[] getValues(Map<String, String[]> paramMap, String key) {
        String[] vs = null;
        for (Entry<String, String[]> kv : paramMap.entrySet()) {
            if (kv.getKey().equalsIgnoreCase(key)) {
                vs = kv.getValue();
                break;
            }
        }
        return vs;
    }

}
