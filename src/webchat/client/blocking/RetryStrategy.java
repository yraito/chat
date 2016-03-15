/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.blocking;

import webchat.core.CommandMessage;

/**
 * Strategy for retrying command that failed
 * @author Edward
 */
public interface RetryStrategy {
    
    
    long getTimeUntilNextTry(CommandMessage cmdMsg, int triesSoFar); 
}
