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

/**
 *
 * @author Nick
 */
public class LogoutCommand extends CommandMessage {

    private final static Logger logger = LoggerFactory.getLogger(LogoutCommand.class);

    public LogoutCommand() {
        super("logout", null, null, null);
    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        ResultMessage result = null;
        if (cs.getUserName() == null) {
            logger.debug("User already logged out");
            result = ResultMessage.success();
        } else {
            logger.debug("Logging out {}", cs.getUserName());
            result = ResultMessage.success();
        }
        cs.closeSession();
        return result;
    }

}
