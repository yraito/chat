package webchat.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import webchat.dao.dto.AdminRecord;
import webchat.dao.dto.UserRecord;
import webchat.servlet.api.HttpServletClientSession;

public class WebAuthenticationFilter implements Filter {

    private final String adminRootPath = "/admin";
    private final String adminLoginPath = "/admin/login";
    private final String userHomePath = "/rooms";
    private final String userLoginPath = "/login";

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

        if (relPath.startsWith(adminRootPath)) {
            AdminRecord adminRecord = AdminLoginLogoutServlet.getAdminRecord(sess);
            boolean isLoginPage = relPath.startsWith(adminLoginPath);
            if (adminRecord == null && !isLoginPage) {
                resp.sendRedirect(adminLoginPath);
            } else if (adminRecord != null && isLoginPage) {
                resp.sendRedirect(adminRootPath);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            UserRecord userRecord = (UserRecord) HttpServletClientSession.getUserRecord(sess);
            boolean isLoginPage = relPath.startsWith(userLoginPath);
            if (userRecord == null && !isLoginPage) {
                resp.sendRedirect(userLoginPath);
            } else if (userRecord != null && isLoginPage) {
                resp.sendRedirect(userHomePath);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
