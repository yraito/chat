/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core.command;

import webchat.core.*;
import static webchat.core.command.AwayCommand.AWAY_START_KEY;

/**
 *
 * @author Edward
 */
public class BackCommand extends CommandMessage {

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        String userNameLower = cs.getUserName().toLowerCase();
        try {
            mgr.getLockManager().acquireLock(userNameLower);
            Object awayStart = cs.detach(AWAY_START_KEY);
            if (awayStart == null) {
                return ResultMessage.success();
            }
            
            mgr.dispatchMessage(this, cs);
            return ResultMessage.success(awayStart.toString());

        } finally {
            mgr.getLockManager().releaseLock(userNameLower);
        }
    }

}
