/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.core.ChatException;
import webchat.core.ChatManager;
import webchat.core.ClientSession;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.dao.dto.UserRecord;
import static webchat.util.StringUtils.*;
import static webchat.core.Checks.*;
import webchat.dao.DaoException;
import webchat.core.Authenticator;

/**
 *
 * @author Nick
 */
public class LoginCommand extends CommandMessage {

    private final static Logger logger = LoggerFactory.getLogger(CommandMessage.class);

    private Authenticator authenticator;

    public LoginCommand(String username, String password) {
        super("login", null, null, null, username, password);
    }

    public LoginCommand(String username, String password, String uuid) {
        super("login", null, null, null, username, password, uuid);
    }

    public LoginCommand() {

    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        String username = getArg(0);
        String password = getArg(1);
        String uuid = getArg(2);
        checkArgs(!isNullOrEmpty(username), "missing username");
        checkArgs(!isNullOrEmpty(password), "missing password");

        UserRecord usrRecord = null;
        ResultMessage result = null;
        Authenticator auth = getAuthenticator(mgr);

        try {
            //lockMgr
            if (isNullOrEmpty(uuid)) {
                usrRecord = auth.authenticateUser(username, password);
            } else {
                usrRecord = auth.authenticateAgent(username, password, uuid);
            }
            //If UserRecord is null, then authentication failed. Respond error
            if (usrRecord == null) {
                logger.debug("Auth failed, sending 401");
                result = ResultMessage.error("Invalid credentials");

            } else {

                Integer currId = cs.getUserId();
                String currName = cs.getUserName();
                //If already logged in under this account, respond success
                if (currId != null && currId.equals(usrRecord.getId())) {
                    logger.debug("Already logged in as {}, sending OK", currName);
                    result = ResultMessage.success();
                    super.persistable = false;
                    //Login successful
                } //If already logged in under a different account, respond error
                else if (currId != null) {
                    logger.debug("Logged in as different user: {} already, sending error", currName);
                    result = ResultMessage.error("Already logged in as " + currName);
                } //If not logged in, notify chat manager of new session and respond success
                else {
                    logger.debug("Login successful as {}", usrRecord.getUsername());
                    //Attach user's record to session

                    cs.setUser(usrRecord);
                    mgr.onNewSession(cs);
                    mgr.dispatchMessage(this, cs);
                    result = ResultMessage.success();
                }
            }
        } catch (DaoException e) {
            logger.error("DaoException authenticating: ", e);
            result = ResultMessage.error(e.getMessage());
        }

        return result;
    }

    private Authenticator getAuthenticator(ChatManager mgr) {
        if (authenticator == null) {
            authenticator = new Authenticator(mgr.getDaoFactory());
        }
        return authenticator;
    }
}
