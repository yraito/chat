package webchat.servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;
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
import webchat.dao.dto.UserRecord;
import webchat.servlet.api.ApiServletListener;
import webchat.servlet.api.HttpServletClientSession;
import webchat.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import webchat.dao.dto.AdminRecord;

/**
 * processRegister takes the following params: processModify takes the following
 * params:
 *
 * @author Nick
 */
@WebServlet(
        name = "UserAccountServlet",
        description = "Handles user/agent account registration, modification, and de/reactivation",
        urlPatterns = {"/processRegister", "/processModify"}
)
public class UserAccountServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(UserAccountServlet.class);

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String relPath = req.getServletPath().toLowerCase();
        
        if (relPath.startsWith("/processregister")) {
            processRegister(req, resp);
        } else if(relPath.startsWith("/processmodify")){
            processModify(req, resp);
        } else {
            throw new ServletException("path");
        }
    }

    private void processRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        boolean isAgent = Boolean.valueOf(req.getParameter("agent"));
        logger.debug("processRegister: username: {}; password: {}; email: {}; isAgent: {}", username, password, email, isAgent);
        
        List<String> errMsgs = new LinkedList<>();
        checkUsername(username, true, errMsgs);
        checkPassword(password, true, errMsgs);
        checkEmail(email, !isAgent, errMsgs);
        if (!errMsgs.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(StringUtils.newString(errMsgs, "<br />"));
            resp.getWriter().close();
            return;
        }
        UserRecord currUser = HttpServletClientSession.getUserRecord(req.getSession());
        if (isAgent && currUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print("Must be logged in to create agent account");
            return;
        }
        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {

            String emailAttr = isAgent ? null : email;
            Integer ownerIdAttr = isAgent ? currUser.getId() : null;
            UserRecord newRecord = createUserAccountIfNotExists(daoConn, username, password, ownerIdAttr, emailAttr);
            if (ownerIdAttr != null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(newRecord.getUuid());
            } else {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (DaoException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        } catch (ServletException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().print(e.getMessage());
        } finally {
            resp.getWriter().close();
        }
    }

    private void processModify(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<String> errMsgs = new LinkedList<>();
        UserRecord currUser = HttpServletClientSession.getUserRecord(req.getSession());
        AdminRecord currAdmin = AdminLoginLogoutServlet.getAdminRecord(req.getSession());
        String idParam = req.getParameter("id");
        if (currUser == null && idParam == null) {
            errMsgs.add("No id specified. Must be logged in as user to modify own account");
        } else if (currAdmin == null && currUser == null && idParam != null) {
            errMsgs.add("Must be logged in as admin or user to modify account with id=" + idParam);
        }
        String activeParam = req.getParameter("delete");
        if (activeParam != null && idParam == null) {
            errMsgs.add("Cannot delete own account");
        }
        
        Boolean delete = Parser.parseBoolean(activeParam, null);
        if (activeParam != null && delete == null) {
            errMsgs.add("Unable to parse parameter \"delete\": " + activeParam);
        }
        if (!errMsgs.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            resp.getWriter().print(StringUtils.newString(errMsgs, "<br />"));
            return;
        }

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            UserRecord modifyUser = null;
            if (idParam != null) {
                Integer userId = Parser.parseInt(idParam);
                if (userId != null) {
                    modifyUser = daoConn.getUserDao().findById(userId);
                }
                if (modifyUser == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().print("No user with id=" + idParam);
                    return;
                } else if (currAdmin == null) {
                    Integer ownerId = modifyUser.getOwnerId();
                    if (ownerId == null || !ownerId.equals(currUser.getId())) {
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.getWriter().print("Not owner of id=" + idParam);
                        return;
                    }
                }
            } else {
                modifyUser = currUser;
            }
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String email = req.getParameter("email");
            boolean isAgent = Boolean.valueOf(req.getParameter("agent"));
            checkUsername(username, false, errMsgs);
            checkEmail(email, false, errMsgs);
            checkPassword(password, false, errMsgs);
            if (!errMsgs.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print(StringUtils.newString(errMsgs, "<br />"));
                return;
            }
            modifyExistingUserAccount(daoConn, modifyUser, username, password, email, delete, isAgent);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        }catch (DaoException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(e.getMessage());
        } catch (ServletException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().print(e.getMessage());
        } finally {
            resp.getWriter().close();
        }

    }

    private void checkUsername(String username, boolean required, List<String> errMsgs) {
        if (required && username == null) {
            errMsgs.add("Missing username parameter");
        } else if (username != null && !usernamePattern.matcher(username).matches()) {
            errMsgs.add("Username must be alphanumeric 4-30 chars");
        }
    }

    private void checkPassword(String password, boolean required, List<String> errMsgs) {
        if (required && password == null) {
            errMsgs.add("Missing password parameter");
        } else if (password != null && !passwordPattern.matcher(password).matches()) {
            errMsgs.add("Password must be 4-30 chars");
        }
    }

    private void checkEmail(String email, boolean required, List<String> errMsgs) {
        if (required && email == null) {
            errMsgs.add("Missing email parameter");
        } else if (email != null && !emailPattern.matcher(email).matches()) {
            errMsgs.add("Invalid email address");
        }
    }

 
    private UserRecord createUserAccountIfNotExists(DaoConnection daoConn, String username, String password, Integer ownerId, String email) throws DaoException, ServletException {
        
        UserRecord existingAcct = daoConn.getUserDao().findActiveByName(username);
        if (existingAcct != null) {
            throw new ServletException("Username " + username + " already exists");
        }
        if (ownerId == null) {
            existingAcct = daoConn.getUserDao().findActiveByEmail(email);
        }
        if (existingAcct != null) {
            throw new ServletException("An account with email " + email + " already exists");
        }

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
            throw new RuntimeException("Runtime exception hashing password", e);
        }
    }

    private UserRecord modifyExistingUserAccount(DaoConnection daoConn, UserRecord prevRecord, String newName, String newPass, String newEmail, Boolean delete, boolean isAgent) throws DaoException, ServletException {

        if (newName != null) {
            UserRecord existingAcct = daoConn.getUserDao().findActiveByName(newName);
            if (existingAcct != null && !Objects.equals(existingAcct.getId(), prevRecord.getId())) {
                throw new ServletException("Username " + newName + " already exists");
            }
            prevRecord.setUsername(newName);
        }
        if (newEmail != null && !isAgent) {
            UserRecord existingAcct = daoConn.getUserDao().findActiveByEmail(newEmail);
            if (existingAcct != null && !Objects.equals(existingAcct.getId(), prevRecord.getId())) {
                throw new ServletException("An account with email " + newEmail + " already exists");
            }
            prevRecord.setEmail(newEmail);
        }
        try {
            if (newPass != null) {
                prevRecord.setPasshash(PasswordHash.createHash(newPass));
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Runtime exception hashing password", e);
        }
        if (delete != null) {
            if (prevRecord.getDestroyed() == null && delete) {
                prevRecord.setDestroyed(System.currentTimeMillis());
            } 
        }
        daoConn.getUserDao().modify(prevRecord);
        return prevRecord;
    }

    public static void main(String[] args) {
        System.out.println("hmv@hhvv.com".matches(".+@.+"));
        System.out.println("hfh@".matches(".+@.+"));
        System.out.println("@hfh".matches(".+@.+"));
        System.out.println("hf.h".matches(".+@.+"));
    }

}
