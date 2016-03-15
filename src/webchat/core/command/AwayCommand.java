/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core.command;

import webchat.core.*;

/**
 *
 * @author Edward
 */
public class AwayCommand extends CommandMessage {

    public static String AWAY_START_KEY = "AWAY_START";

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        String userNameLower = cs.getUserName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(userNameLower);
            Object awayStart = cs.getAttachment(AWAY_START_KEY);
            if (awayStart == null) {
                awayStart = System.currentTimeMillis();
                cs.attach("AWAY_START", awayStart);
            }
            clearOtherArgs();
            addArg(awayStart.toString());
            mgr.dispatchMessage(this, cs);
            return ResultMessage.success(awayStart.toString());

        } finally {
            mgr.getLockManager().releaseLock(userNameLower);
        }
    }

}
