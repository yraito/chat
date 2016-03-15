package webchat.core;

/**
 * A message from the server to the client in response to a CommandMessage.
 * <br />
 *
 * @author Nick
 *
 */
public class ResultMessage implements Message {

    public static ResultMessage success(Object o) {
        return new ResultMessage(ResultType.SUCCESS, null, null, o);
    }

    public static ResultMessage success() {
        return new ResultMessage(ResultType.SUCCESS, null, null, null);
    }

    public static ResultMessage error(String err) {
        return new ResultMessage(ResultType.FAILURE, ErrorCode.OTHER, err, null);
    }

    public static ResultMessage error(ErrorCode ec, String err) {
        return new ResultMessage(ResultType.FAILURE, ec, err, null);
    }

    public enum ResultType {
        SUCCESS, FAILURE
    }

    public enum ErrorCode {
        BAD_CREDENTIALS, NOT_LOGGED_IN, INVALID_SYNTAX, INVALID_STATE, INTERNAL, OTHER
    }

    private ResultType type;
    private String err;
    private Object res;
    private ErrorCode ec;

    private ResultMessage(ResultType type, ErrorCode ec, String err, Object res) {
        this.type = type;
        this.ec = ec;
        this.err = err;
        this.res = res;
    }

    /**
     * Is this an error message (ie did the command fail)?
     *
     * @return true if error occured
     */
    public boolean isError() {
        return type == ResultType.FAILURE;
    }

    /**
     * Get the error message, or null if this is not an error type result
     *
     * @return
     */
    public String getError() {
        return err;
    }

    public ErrorCode getErrorCode() {
        return ec;
    }

    /**
     * The return value of the (successful) command if there was one
     *
     * @return
     */
    public Object getResult() {
        return res;
    }

    public ResultType getResultType() {
        return type;
    }

    @Override
    public String toString() {
        if (isError()) {
            return "<ERROR>" + getError() + "</ERROR>";
        } else {
            return "<SUCCESS>" + getResult() + "</SUCCESS>";
        }
    }

}
