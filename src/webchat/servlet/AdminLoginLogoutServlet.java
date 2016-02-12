package webchat.servlet;

import webchat.core.Authenticator;
import static webchat.servlet.api.ApiServletListener.*;
import static webchat.util.StringUtils.*;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.dao.DaoConnectionFactory;
import webchat.dao.dto.AdminRecord;

@WebServlet(
        urlPatterns = {"/admin/login", "/admin/processLogin", "/admin/processLogout"}
)
public class AdminLoginLogoutServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(AdminLoginLogoutServlet.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String ADMIN_AUTH = "admin_auth";

    Authenticator authenticator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        DaoConnectionFactory daoFactory = (DaoConnectionFactory) config.getServletContext().getAttribute(DAO_FACTORY_KEY);
        authenticator = new Authenticator(daoFactory);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String relPath = req.getServletPath().toLowerCase();
        if (relPath.startsWith("/admin/login")) {
            req.getRequestDispatcher("/WEB-INF/adminlogin.jsp").forward(req, resp);
        } else if (relPath.startsWith("/admin/processlogin")) {
            processLogin(req, resp);
        } else if (relPath.startsWith("/admin/processlogout")) {
            processLogout(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String relPath = req.getServletPath().toLowerCase();
        if (relPath.startsWith("/admin/login")) {
            req.getRequestDispatcher("/WEB-INF/adminlogin.jsp").forward(req, resp);
        } else if (relPath.startsWith("/admin/processlogout")) {
            processLogout(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    protected void processLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Admin login servlet: received request at {}", req.getServletPath());

        String usr = req.getParameter("username");
        String pass = req.getParameter("password");
        AdminRecord usrRecord = null;

        //If username and password supplied in request, check validity against database
        if (!isNullOrEmpty(usr) && !isNullOrEmpty(pass)) {
            usrRecord = authenticator.authenticateAdmin(usr, pass);
        }
        //If UserRecord is null, then credentials were invalid. Respond error
        if (usrRecord == null) {
            logger.debug("Auth failed, sending 401");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print("Invalid credentials");
            resp.getWriter().close();
        } //Credentials were valid
        else {
            //If already logged in under a different account, respond ok, but remain
            //logged into original account
            Object currId = req.getSession().getAttribute(ADMIN_AUTH);
            if (currId != null && !((AdminRecord) currId).getId().equals(usrRecord.getId())) {
                logger.debug("Already logged in as {}", usrRecord.getUsername());

                //Login successful
            } else {
                logger.debug("Login successful as {}", usrRecord.getUsername());
                req.getSession().setAttribute(ADMIN_AUTH, usrRecord);
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.getWriter().close();
        }
    }

    private void processLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession httpSess = req.getSession(false);
            if (httpSess != null) {
                httpSess.invalidate();
            }
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        resp.getWriter().close();
    }

}
