package webchat.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.dao.dto.AdminRecord;
import webchat.dao.dto.UserRecord;
import webchat.servlet.api.HttpServletClientSession;

@WebFilter(
        filterName="RedirectToLoginFilter",
        urlPatterns = {"/*"}
)
public class WebAuthenticationFilter implements Filter {

    
    private final static Logger logger = LoggerFactory.getLogger(WebAuthenticationFilter.class);

    private final String adminRootPath = "/admin/";
    private final String userRootPath = "/web/";
    private final String apiRootPath = "/api/";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession sess = req.getSession();
        String relPath = req.getServletPath().toLowerCase();
        logger.debug("Filter relPath: {}", relPath);
        if (relPath.startsWith(apiRootPath)) {
            logger.debug("Api request, passing through");
            chain.doFilter(request, response);
        } else if (relPath.startsWith(adminRootPath)) {
            AdminRecord adminRecord = AdminLoginLogoutServlet.getAdminRecord(sess);
            redirectToOrFromLoginPage(adminRootPath, adminRecord, req, resp, chain);
        } else if (relPath.startsWith(userRootPath)){
            UserRecord userRecord = (UserRecord) HttpServletClientSession.getUserRecord(sess);
            redirectToOrFromLoginPage(userRootPath, userRecord, req, resp, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void redirectToOrFromLoginPage(String rootPath, Object auth, HttpServletRequest req, HttpServletResponse resp, FilterChain chain) 
        throws IOException, ServletException{
        String relPath = req.getServletPath().toLowerCase();
        boolean isLoginPage = relPath.startsWith(rootPath + "login");
        boolean isRegisterPage =  relPath.startsWith(rootPath + "register");
        boolean isProcessLogin = relPath.startsWith(rootPath + "processlogin");
        boolean isProcessLogout = relPath.startsWith(rootPath + "processlogout");
        boolean isLoginOrLogout = isLoginPage || isRegisterPage || isProcessLogin || isProcessLogout;
        if (auth == null && !isLoginOrLogout) {
            logger.debug("Auth: {}. Redirecting to login page: {}", auth, rootPath + "login");
            resp.sendRedirect(req.getServletContext().getContextPath()  + rootPath + "login");
        } else if (auth != null && isLoginPage) {
            logger.debug("Redirecting away from login page to: {}", rootPath);
            resp.sendRedirect(req.getServletContext().getContextPath()  + rootPath);
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
