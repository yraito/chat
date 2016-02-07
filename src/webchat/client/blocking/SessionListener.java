/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.blocking;

/**
 *
 * @author Nick
 */
public interface SessionListener {
    
    default void onJoinChannel(BlockingChannel channel) {  }
    
    default void onLeaveChannel(BlockingChannel channel) {  }
    
    default void onDisconnect() { }
}
