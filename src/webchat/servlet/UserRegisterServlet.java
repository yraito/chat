package webchat.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.LinkedList;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.dao.Searcher;
import webchat.dao.Where;
import webchat.dao.dto.UserRecord;
import webchat.servlet.api.ApiServletListener;
import webchat.servlet.api.HttpServletClientSession;
import webchat.util.*;

@WebServlet(
        name = "UserRegisterServlet",
        description = "Handles user and agent registration",
        urlPatterns = {"/register", "/processRegister"}
)
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
        usernamePattern = Pattern.compile("[a-zA-Z[0-9]]{4,30}"); //alphanumeric, 4-30 chars
        passwordPattern = Pattern.compile(".{4,30}"); //4 - 30 chars
        emailPattern = Pattern.compile(".+@.+"); //@, with at least one char on either side
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath().toLowerCase();
        if (path.startsWith("/register")) {
            req.getServletContext().getRequestDispatcher("/WEB-INF/register.jsp").forward(req, resp);
        } else if (path.startsWith("/processregister")) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath().toLowerCase();
        if (path.startsWith("/register")) {
            req.getServletContext().getRequestDispatcher("/WEB-INF/register.jsp").forward(req, resp);
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        boolean isAgent = Boolean.valueOf(req.getParameter("agent"));
        UserRecord currUser = HttpServletClientSession.getUserRecord(req.getSession());
        List<String> errMsgs = new LinkedList<>();
        req.setAttribute("reg_errors", errMsgs);
        if (username == null || !usernamePattern.matcher(username).matches()) {
            errMsgs.add("Username must be alphanumeric 4-30 chars");
        }
        if (password == null || !passwordPattern.matcher(password).matches()) {
            errMsgs.add("Password must be 4-30 chars");
        }
        if (!isAgent && (email == null || !emailPattern.matcher(email).matches())) {
            errMsgs.add("Invalid email address");
        }
        if (isAgent && currUser == null) {
            errMsgs.add("Must be logged in to create agent account");
        }
        if (errMsgs.isEmpty()) {
            String emailAttr = isAgent ? null : email;
            Integer ownerIdAttr = isAgent ? currUser.getId() : null;
            UserRecord newUsrRecord = null;
            Exception exception = null;
            try (final DaoConnection daoConn = daoFactory.openDaoConnection()) {
                newUsrRecord = (UserRecord) daoConn.executeTransaction(c -> {
                    return createUserAccountIfNotExists(c, username, password, ownerIdAttr, emailAttr);
                });
            } catch (DaoException | RuntimeException e) {
                //log
                exception = e;
            }
            if (newUsrRecord != null) {
                respondSuccess(newUsrRecord, resp);
            } else {
                respondException(exception, resp);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(StringUtils.newString(errMsgs, "<br />"));
            resp.getWriter().close();
        }

    }

    private void respondSuccess(UserRecord ur, HttpServletResponse resp) throws IOException {
        if (ur.getUuid() != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(ur.getUuid());
            resp.getWriter().close();
        } else {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    private void respondException(Exception e, HttpServletResponse resp) throws IOException {
        if (e instanceof IllegalArgumentException) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        } else { 
            resp.setStatus(500);
        }
        resp.getWriter().print(e.getMessage());
        resp.getWriter().close();
    }
    
    private UserRecord createUserAccountIfNotExists(DaoConnection daoConn, String username, String password, Integer ownerId, String email) throws DaoException {
        UserRecord existingAcct = daoConn.getUserDao().find(username);
        if (existingAcct != null) {
            throw new IllegalArgumentException("Username " + username + " already exists");
        }

        try {
            Searcher<UserRecord> searcher = daoConn.getUserDao().searcher();
            Field emailField = UserRecord.class.getDeclaredField("email");
            Where<UserRecord> where = searcher.whereLwrEq(emailField, email);
            List<UserRecord> users = where.doQuery();
            System.out.println(users.size());
            if (!users.isEmpty()) {
                throw new IllegalArgumentException("An account with email " + email + " already exists");
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("NoSuchField: email", e);
        }

        //enforce unique email?
        try {
            UserRecord newAcct = new UserRecord();
            newAcct.setUsername(username);
            newAcct.setPasshash(PasswordHash.createHash(password));
            newAcct.setEmail(email);
            newAcct.setOwnerId(ownerId);
            newAcct.setCreated(System.currentTimeMillis());
            if (ownerId != null) {
                newAcct.setUuid(UUID.randomUUID().toString());
            }
            daoConn.getUserDao().save(newAcct);
            return newAcct;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("hmv@hhvv.com".matches(".+@.+"));
        System.out.println("hfh@".matches(".+@.+"));
        System.out.println("@hfh".matches(".+@.+"));
        System.out.println("hf.h".matches(".+@.+"));
    }

}
