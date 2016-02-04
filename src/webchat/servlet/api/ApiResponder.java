package webchat.servlet.api;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import webchat.core.Message;
import webchat.core.ResultMessage;

/*
 respond 200 and flag errors in body? error codes?
*/
public class ApiResponder {

    private static Formatter formatter = new XStreamFormatter();

    public static ApiResponder get(HttpServletRequest req, HttpServletResponse resp) {
        return new ApiResponder(resp);
    }

    HttpServletResponse resp;

    private ApiResponder(HttpServletResponse resp) {
        this.resp = resp;
    }

    public void respond(ResultMessage rm) throws IOException {
        respondOK(rm);
    }

    public void respondClientError(String errMsg) throws IOException{
        ResultMessage rm = ResultMessage.error("Client error: " + errMsg);
        respondOK(rm);
    }

    public void respondServerError(String errMsg) throws IOException {
        ResultMessage rm = ResultMessage.error("Server error: " + errMsg);
        respondOK(rm);
    }

    public void respondUnauthorized(String errMsg) throws IOException {
        ResultMessage rm = ResultMessage.error("Unauthorized: " + errMsg);
        respondOK(rm);
    }

    public void respondOK(Message rm) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        OutputStream os = resp.getOutputStream();
        formatter.writeMessage(rm, os);
        os.flush();
        os.close();
    }
}
