package webchat.core;

import java.util.List;

/**
 * Static convenience methods for checking conditions before executing a
 * command. E.g. when the server receives a command like "whisper to X in room
 * Y", it needs to first check that the room exists, and that the sender and
 * recipient are both in that room.
 *
 * @author Nick
 *
 */
public class Checks {

    /**
     * Checks whether a list of arguments has the correct number of elements,
     * and whether each element has an acceptable length. Throws an exception if
     * either condition is violated
     *
     * @param n
     * @param l
     * @param res
     * @throws ChatException
     */
    public static void checkFormat(int n, List<String> l, int... res) throws ChatException {
        if (l.size() < n) {
            throw new ChatException("Bad syntax (incorrect # of args)");
        }
        for (int j = 0; j < l.size(); j += 2) {
            if (l.get(j).length() < res[j]) {
                throw new ChatException("Bad syntax (arg too short)");
            }
            if (l.get(j).length() > res[j + 1]) {
                throw new ChatException("Bad syntax (arg too long)");
            }
        }
    }

    /**
     * Shorthand condition check. Throws an exception if passed condition is
     * false.
     *
     * @param b
     * @param err
     * @throws ChatException
     */
    public static void checkArgs(boolean b, String err) throws ChatException {
        if (!b) {
            throw new ChatException("Bad syntax: " + err);
        }
    }

    /**
     * Shorthand condition check. Throws an exception if passed condition is
     * false. Meant for checking argument validity. May be changed to throw a
     * more specific exception
     *
     * @param b
     * @param err
     * @throws ChatException
     */
    public static void checkArgs(boolean b) throws ChatException {
        checkArgs(b, "");
    }

    /**
     * Shorthand condition check. Meant for checking authorization.
     *
     * @param b
     * @param err
     * @throws ChatException
     */
    public static void checkAuthorized(boolean b, String err) throws ChatException {
        if (!b) {
            throw new ChatException("Not authorized: " + err);
        }
    }

    /**
     * Shorthand condition check. Meant for checking state, e.g. on a create
     * room command, checking that the room doesn't already exist
     *
     * @param b
     * @param err
     * @throws ChatException
     */
    public static void checkState(boolean b, String err) throws ChatException {
        if (!b) {
            throw new ChatException("Invalid state: " + err);
        }
    }

    /**
     * Does a room with this name exist?
     *
     * @param mgr
     * @param room
     * @return
     */
    public static boolean roomExists(ChatManager mgr, String room) {
        return mgr.getRoom(room) != null;
    }

    /**
     * Is User "user" in Room "room"?
     *
     * @param mgr
     * @param user
     * @param room
     * @return
     */
    public static boolean userIsInRoom(ChatManager mgr, String user, String room) {
        return mgr.getRoom(room).hasUser(user);
    }

    /**
     * Is the given user the owner or a token holder in this room?
     *
     * @param mgr
     * @param user
     * @param room
     * @return
     */
    public static boolean userHasPower(ChatManager mgr, String user, String room) {
        RoomBean r = mgr.getRoom(room);
        user = user.toLowerCase();
        return r.getOwner().equalsIgnoreCase(user) || r.getTokenHolders().contains(user);
    }

}
