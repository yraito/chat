package webchat.servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.LinkedList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.dao.dto.UserRecord;
import webchat.servlet.api.ApiServletListener;
import webchat.servlet.api.HttpServletClientSession;
import webchat.util.PasswordHash;

public class UserRegisterServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	DaoConnectionFactory daoFactory;
	Pattern usernamePattern;
	Pattern passwordPattern;
	Pattern emailPattern;
	

	@Override
	public void init(ServletConfig config) throws ServletException {
		daoFactory = (DaoConnectionFactory) config.getServletContext().getAttribute(ApiServletListener.DAO_FACTORY_KEY);
		usernamePattern = Pattern.compile("[a-zA-Z[0-9]]{4, 30}"); //alphanumeric, 4-30 chars
		passwordPattern = Pattern.compile(".{4, 30}"); //4 - 30 chars
		emailPattern = Pattern.compile(".+@.+"); //@, with at least one char on either side
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
                String path = req.getServletPath().toLowerCase();
		
		if (path.startsWith("/register")) {
			req.getServletContext().getRequestDispatcher("register.jsp").forward(req, resp);
                        return;
		} 
                String username = req.getParameter("username");
		String password = req.getParameter("password");
		String email = req.getParameter("email");
		boolean isAgent = req.getParameter("agent") != null;
		UserRecord currUser = HttpServletClientSession.getUserRecord(req.getSession());
                List<String> errMsgs = new LinkedList<>();
                req.setAttribute("reg_errors", errMsgs);
		if (username == null || !usernamePattern.matcher(username).matches()) {
			errMsgs.add("Username must be alphanumeric 4-30 chars");
		} 
                if (password == null || !passwordPattern.matcher(password).matches()) {
			errMsgs.add("Password must be 4-30 chars");
		}
                if (isAgent && (email == null || !emailPattern.matcher(email).matches())) {
			errMsgs.add("Invalid email address");
		}
                if (isAgent && currUser ==  null) {
			errMsgs.add("Must be logged in to create agent account");
		} 
                if (errMsgs.isEmpty()){
			String emailAttr = isAgent ? email : null;
			Integer ownerIdAttr = isAgent ? null : currUser.getId();
			Boolean success = null;
			try (final DaoConnection daoConn = daoFactory.openDaoConnection()) {
				success = (Boolean) daoConn.executeTransaction( c -> {
					return createUserAccountIfNotExists(c, username, password, ownerIdAttr, emailAttr);
				});
			} catch (DaoException e) {
                            errMsgs.add(e.getMessage());
                        }
			if (success != null && !success){
                            errMsgs.add("Username " + username + " already exists");
			}
		}
                if (errMsgs.isEmpty()) {
                    dispatchSuccess(req, resp, isAgent);
                } else {
                    dispatchError(req, resp, isAgent);
                }
	}

        private void dispatchSuccess(HttpServletRequest req, HttpServletResponse resp, boolean agent) throws IOException, ServletException {
            String view = agent ? "account.jsp" : "login.jsp";
            RequestDispatcher rd = req.getRequestDispatcher(view);
            if (rd == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                rd.forward(req, resp);
            }
        }
        
        private void dispatchError(HttpServletRequest req, HttpServletResponse resp, boolean agent) throws IOException, ServletException {
            String view = agent ? "account.jsp" : "register.jsp";
            RequestDispatcher rd = req.getRequestDispatcher(view);
            if (rd == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                rd.forward(req, resp);
            }
        }
	private boolean createUserAccountIfNotExists(DaoConnection daoConn, String username, String password, Integer ownerId, String email) throws DaoException {
		UserRecord existingAcct = daoConn.getUserDao().find(username);
		if (existingAcct != null) {
			return false;
		} 

		try {
			UserRecord newAcct = new UserRecord();
			newAcct.setUsername(username);
			newAcct.setPasshash( PasswordHash.createHash(password) );
			newAcct.setEmail(email);
			newAcct.setOwnerId(ownerId);
			newAcct.setCreated(System.currentTimeMillis());
			daoConn.getUserDao().save(newAcct);
			return true;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}


}
