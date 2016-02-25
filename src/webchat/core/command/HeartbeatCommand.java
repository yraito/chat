/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core.command;

import webchat.core.*;
/**
 *
 * @author Nick
 */
public class HeartbeatCommand extends CommandMessage {

    public HeartbeatCommand() {
        super("heartbeat", null, null, null);
        super.persistable = false;
    }

    @Override
    public ResultMessage execute(ClientSession cs, ChatManager mgr) throws ChatException {
        mgr.dispatchMessage(this, cs);
        return ResultMessage.success();
    }
    
}
