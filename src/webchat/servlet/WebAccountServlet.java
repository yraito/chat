package webchat.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.RequestDispatcher;
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
import webchat.dao.sql.MySQLDaoConnectionFactory;
import webchat.servlet.api.ApiServletListener;
import webchat.servlet.api.HttpServletClientSession;

public class WebAccountServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private DaoConnectionFactory daoFactory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        daoFactory = (DaoConnectionFactory) config.getServletContext().getAttribute(ApiServletListener.DAO_FACTORY_KEY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserRecord userRecord = HttpServletClientSession.getUserRecord(req.getSession());
        if (userRecord == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {

            Searcher<UserRecord> searcher = daoConn.getUserDao().searcher();
            Field ownerField = UserRecord.class.getDeclaredField("ownerId");
            Integer userId = userRecord.getId();
            Where<UserRecord> where = searcher.whereEq(ownerField, userId);
            List<UserRecord> agents = where.doQuery();

            req.setAttribute("userrecords", userRecord);
            req.setAttribute("agentrecords", agents);
            req.getRequestDispatcher("account.jsp").forward(req, resp);;
        } catch (DaoException | NoSuchFieldException | SecurityException e) {
            throw new ServletException(e);
        }
    }

}
