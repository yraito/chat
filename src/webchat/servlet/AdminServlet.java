package webchat.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.dao.Searcher;
import webchat.dao.Where;
import webchat.dao.dto.EventRecord;
import webchat.dao.dto.UserRecord;
import webchat.dao.sql.MySQLDaoConnectionFactory;
import webchat.util.FromParamsSearcher;
import static webchat.util.ParamParser.*;

@WebServlet(
        urlPatterns = {"/admin/messages", "/admin/events", "/admin/users", "/admin", "/admin/"}
)
public class AdminServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private DaoConnectionFactory daoFactory = MySQLDaoConnectionFactory.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String relativePath = req.getServletPath().toLowerCase();
        if (relativePath.equals("/admin")) {
            //req.getRequestDispatcher("/WEB-INF/adminmain.jsp").forward(req, resp);
            //resp.sendRedirect("/WEB-INF/messages");
            //return;
        }
        List<?> queryResults = null;
        String viewPath = null;
        if (relativePath.startsWith("/admin/messages")) {
            queryResults = searchMessages(req, resp);
            viewPath = "/WEB-INF/messagesearch.jsp";
        } else if (relativePath.startsWith("/admin/events")) {
            queryResults = searchEvents(req, resp);
            viewPath = "/WEB-INF/eventsearch.jsp";
        } else if (relativePath.startsWith("/admin/users")) {
            queryResults = searchUsers(req, resp);
            viewPath = "/WEB-INF/usersearch.jsp";
        } else {
            resp.sendRedirect(req.getServletContext().getContextPath() + "/admin/messages");
            return;
        }
        req.setAttribute("records", queryResults);
        req.getRequestDispatcher(viewPath).forward(req, resp);

    }

    private List<EventRecord> searchMessages(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] keywords = parseArray(req.getParameter("keywords[]"));
        boolean keywordsAll = parseBoolean(req.getParameter("keywordsall"), false);
        String[] emoticons = req.getParameterValues("emoticons[]");
        boolean emoticonsAll = parseBoolean(req.getParameter("emoticonsall"), false);
        String[] roomNames = parseArray(req.getParameter("roomnames[]"));
        String[] sourceNames = parseArray(req.getParameter("sourcenames[]"));
        String[] targetNames = parseArray(req.getParameter("targetnames[]"));
        String[] messageTypes = req.getParameterValues("types[]");
        if (messageTypes == null || messageTypes.length == 0) {
            messageTypes = new String[]{"message", "whisper"};
        }
        Long startDate = parseDateTime(req.getParameter("startdate"));
        Long endDate = parseDateTime(req.getParameter("enddate"));

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<EventRecord> searcher = daoConn.getEventDao().searcher();
            FromParamsSearcher<EventRecord> fps = new FromParamsSearcher(searcher, EventRecord.class);
            Where<EventRecord> msgWhere = fps.whereContains("message", keywordsAll, keywords);
            Where<EventRecord> emoWhere = fps.whereContains("message", emoticonsAll, emoticons);
            Where<EventRecord> roomWhere = fps.whereLwrEqAny("roomName", roomNames);
            Where<EventRecord> srcWhere = fps.whereLwrEqAny("sourceName", sourceNames);
            Where<EventRecord> tgtWhere = fps.whereLwrEqAny("targetName", targetNames);
            Where<EventRecord> typeWhere = fps.whereLwrEqAny("type", messageTypes);
            Where<EventRecord> dateWhere = fps.whereBtw("timestamp", startDate, endDate);
            Where<EventRecord> fullWhere = msgWhere.and(emoWhere).and(roomWhere).and(srcWhere).and(tgtWhere)
                    .and(typeWhere).and(dateWhere);
            return doQuery("messages", fullWhere, EventRecord.class, "timestamp", req, resp);

        }
    }

    private List<EventRecord> searchEvents(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] roomNames = parseArray(req.getParameter("roomnames[]"));
        String[] sourceNames = parseArray(req.getParameter("sourcenames[]"));
        String[] targetNames = parseArray(req.getParameter("targetnames[]"));
        String[] eventTypes = req.getParameterValues("types[]");
        Long startDate = parseDateTime(req.getParameter("startdate"));
        Long endDate = parseDateTime(req.getParameter("enddate"));

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<EventRecord> searcher = daoConn.getEventDao().searcher();
            FromParamsSearcher<EventRecord> fps = new FromParamsSearcher(searcher, EventRecord.class);
            Where<EventRecord> roomWhere = fps.whereLwrEqAny("roomName", roomNames);
            Where<EventRecord> srcWhere = fps.whereLwrEqAny("sourceName", sourceNames);
            Where<EventRecord> tgtWhere = fps.whereLwrEqAny("targetName", targetNames);
            Where<EventRecord> typeWhere = fps.whereLwrEqAny("type", eventTypes);
            Where<EventRecord> dateWhere = fps.whereBtw("timestamp", startDate, endDate);
            Where<EventRecord> fullWhere = roomWhere.and(srcWhere).and(tgtWhere).and(typeWhere).and(dateWhere);
            fullWhere = fullWhere.and(fps.whereNeqAll("type", "message", "whisper"));
            return doQuery("events", fullWhere, EventRecord.class, "timestamp", req, resp);
        }

    }

    private List<UserRecord> searchUsers(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] usernames = parseArray(req.getParameter("usernames[]"));
        String[] ownerNames = parseArray(req.getParameter("ownernames[]"));
        String[] acctTypes = req.getParameterValues("types[]");
        String[] acctStatuses = req.getParameterValues("statuses[]");
        Long startCreateDate = parseDateTime(req.getParameter("startdate"));
        Long endCreateDate = parseDateTime(req.getParameter("enddate"));

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<UserRecord> searcher = daoConn.getUserDao().searcher();
            FromParamsSearcher<UserRecord> fps = new FromParamsSearcher(searcher, UserRecord.class);
            Where<UserRecord> userWhere = fps.whereLwrEqAny("username", usernames);
            Where<UserRecord> ownerWhere = fps.whereLwrEqAny("ownerName", ownerNames);
            Where<UserRecord> dateWhere = fps.whereBtw("created", startCreateDate, endCreateDate);
            Where<UserRecord> fullWhere = userWhere.and(ownerWhere).and(dateWhere);
            if (acctTypes != null && acctTypes.length == 1) {
                Where<UserRecord> typeWhere = acctTypes[0].equals("agent") ? fps.whereNotNull("ownerId") : fps.whereNull("ownerId");
                fullWhere = fullWhere.and(typeWhere);
            }

            if (acctStatuses != null && acctStatuses.length == 1) {
                Where<UserRecord> statusWhere = acctStatuses[0].equals("inactive") ? fps.whereNotNull("destroyed") : fps.whereNull("destroyed");
                fullWhere = fullWhere.and(statusWhere);
            }
            
            return doQuery("users", fullWhere, UserRecord.class, "username", req, resp);

        }
    }

    private <T> List<T> doQuery(String baseUrl, Where<T> fullWhere, Class<T> clazz, String defaultSortField, HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        Field sortBy = parseField(clazz, req.getParameter("sortby"), defaultSortField);
        int startIndex = parseInt(req.getParameter("start"), 0, Integer.MAX_VALUE, 0);
        int perPage = parseInt(req.getParameter("perpage"), 1, 500, 25);
        int maxRecords = 1000;
        List<T> records = fullWhere.doQuery(sortBy, 0, maxRecords);
        if (startIndex >= records.size()) {
            return Collections.emptyList();
        }
        int endIndex = Math.min(startIndex + perPage, records.size());
        //String query = req.getQueryString();
        //Pages pages = new Pages(baseUrl, query, records.size(), perPage, startIndex);
        req.setAttribute("startIndex", startIndex);
        req.setAttribute("numResults", records.size());
        req.setAttribute("perPage", perPage);
        //req.setAttribute("pages", pages);
        return records.subList(startIndex, endIndex);
    }
    //TODO optimize and, finish doquery, update MySQLUserDao
}
