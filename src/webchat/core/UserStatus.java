/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core;

/**
 *
 * @author Nick
 */
public enum UserStatus {
    
    ONLINE("ONLINE"), 
    AWAY("AWAY"), 
    BUSY("BUSY"),
    WAITING("WAITING"),
    OFFLINE("OFFLINE");

    public static UserStatus fromString(String string) {
            for (UserStatus us : UserStatus.values()) {
                    if (us.string.equalsIgnoreCase(string)) {
                            return us;
                    }
            }
            return null;
    }

    private final String string;

    private UserStatus(String string) {
            this.string = string;;
    }

    public String getString() {
            return string;
    }
}
