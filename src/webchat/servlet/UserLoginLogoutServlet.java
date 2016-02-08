package webchat.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.core.ChatManager;
import webchat.core.ResultMessage;
import webchat.core.command.LoginCommand;

import webchat.servlet.api.ApiServletListener;
import webchat.servlet.api.HttpServletClientSession;


/**
 *
 * @author Nick
 *
 */
public class UserLoginLogoutServlet extends HttpServlet {

	private final static Logger logger = LoggerFactory.getLogger(UserLoginLogoutServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String path = req.getServletPath().toLowerCase();
		
		logger.debug("LoginLogout servlet: received request at {}", path);
		if (path.startsWith("/login")) {
			req.getServletContext().getRequestDispatcher("login.jsp").forward(req, resp);
		} else if (path.startsWith("/processlogin")) {
			loginWeb(req, resp);
		} else if (path.startsWith("/processlogout")){
			logoutWeb(req, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	/**
	 * Handle login of users.
	 * 
	 * @param req
	 * @param resp
	 * @param usrRecord
	 * @throws ServletException
	 * @throws IOException
	 */
	private void loginWeb(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String usr = req.getParameter("username");
		String pass = req.getParameter("password");
		ChatManager chatMgr = (ChatManager) req.getServletContext().getAttribute(ApiServletListener.CHAT_MANAGER_KEY);
                HttpServletClientSession clientSess = new HttpServletClientSession(req.getSession());
                LoginCommand loginCmd = new LoginCommand(usr, pass);
                ResultMessage rslt = chatMgr.processMessage(clientSess, loginCmd);
                if (rslt.isError()) {
                    req.setAttribute("login_error", rslt.getError());
                    req.getRequestDispatcher("login.jsp").forward(req, resp);
                } else {
                    req.getRequestDispatcher("rooms.jsp").forward(req, resp);
                }
	}
	
	
	/**
	 * Handle graceful logout of users
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void logoutWeb(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.isRequestedSessionIdValid()) {
			req.getSession(false).invalidate();
		} 
		req.getServletContext().getRequestDispatcher("login.jsp").forward(req, resp);
	}
	
}
