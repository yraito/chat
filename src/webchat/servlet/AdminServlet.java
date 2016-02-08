package webchat.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.dao.Searcher;
import webchat.dao.Where;
import webchat.dao.dto.EventRecord;
import webchat.dao.dto.UserRecord;
import webchat.dao.sql.MySQLDaoConnectionFactory;
import webchat.util.DaoUtils;

import static webchat.util.StringUtils.*;

public class AdminServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static Field parseField(Class<?> clazz, String fieldName, String defaultFieldName) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        for (Field field : fields) {
            if (field.getName().equals(defaultFieldName)) {
                return field;
            }
        }
        throw new RuntimeException("Default field name \"" + defaultFieldName + "\" is not valid");
    }

    private static int parseInt(String intStr, int min, int max, int defaultValue) {
        if (intStr == null) {
            return defaultValue;
        }
        try {
            int value = Integer.valueOf(intStr);
            value = Math.max(min, value);
            value = Math.min(max, value);
            return value;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Long parseDate(String dateStr, long defaultValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateStr).getTime();
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private static <T> Where<T> whereContainsAnyOrAll(Searcher<T> s, Class<T> c, String f, String a, String... ks) {

        try {
            Field fld = c.getDeclaredField(f);
            Function<String, Where<T>> func = t -> {
                return s.whereLike(fld, '%' + t + '%');
            };
            if (a == null || a.equals("all")) {
                return DaoUtils.whereAll(s, func, ks);
            } else {
                return DaoUtils.whereAny(s, func, ks);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Where<T> whereMatchesAny(Searcher<T> s, Class<T> c, String f, String... ks) {
        try {
            Field fld = c.getDeclaredField(f);
            Function<String, Where<T>> func = t -> {
                if (t.contains("*")) {
                    t = t.replace('*', '%');
                    return s.whereLike(fld, t);
                } else {
                    return s.whereLwrEq(fld, t);
                }
            };
            return DaoUtils.whereAny(s, func, ks);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

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
        if (relativePath.equals("/")) {
            req.getRequestDispatcher("main.jsp").forward(req, resp);
        }
        List<?> queryResults = null;
        String viewPath = null;
        if (relativePath.startsWith("/messages")) {
            queryResults = searchMessages(req, resp);
            viewPath = "messages.jsp";
        } else if (relativePath.startsWith("/events")) {
            queryResults = searchEvents(req, resp);
            viewPath = "events.jsp";
        } else if (relativePath.startsWith("/users")) {
            queryResults = searchUsers(req, resp);
            viewPath = "users.jsp";
        }
        if (viewPath != null) {
            req.setAttribute("results", queryResults);
            req.getRequestDispatcher(viewPath).forward(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private List<EventRecord> searchMessages(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] keywords = splitQuoted(req.getParameter("keywords"));
        String keywordQualifier = req.getParameter("keywordqualifier");
        String[] emoticons = req.getParameterValues("emoticons");
        String emoticonQualifier = req.getParameter("emoticonqualifier");
        String[] roomNames = splitQuoted(req.getParameter("roomNames"));
        String[] sourceNames = splitQuoted(req.getParameter("sourcenames"));
        String[] targetNames = splitQuoted(req.getParameter("targetnames"));
        String[] messageTypes = req.getParameterValues("types");
        if (messageTypes == null || messageTypes.length == 0) {
            messageTypes = new String[]{"message", "whisper"};
        }
        Long startDate = parseDate(req.getParameter("startdate"), 1L);
        Long endDate = parseDate(req.getParameter("enddate"), System.currentTimeMillis());
        Field sortBy = parseField(EventRecord.class, req.getParameter("sortby"), "timestamp");
        int startIndex = parseInt(req.getParameter("start"), 0, Integer.MAX_VALUE, 0);
        int maxRecords = 1000;
        Class<EventRecord> clazz = EventRecord.class;

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<EventRecord> searcher = daoConn.getEventDao().searcher();
            Where<EventRecord> msgWhere = whereContainsAnyOrAll(searcher, clazz, "message", keywordQualifier, keywords);
            Where<EventRecord> emoWhere = whereContainsAnyOrAll(searcher, clazz, "message", emoticonQualifier, emoticons);
            Where<EventRecord> roomWhere = whereMatchesAny(searcher, clazz, "room", roomNames);
            Where<EventRecord> srcWhere = whereMatchesAny(searcher, clazz, "sourceName", sourceNames);
            Where<EventRecord> tgtWhere = whereMatchesAny(searcher, clazz, "targetName", targetNames);
            Where<EventRecord> typeWhere = whereMatchesAny(searcher, clazz, "type", messageTypes);
            Where<EventRecord> dateWhere = searcher.whereBtw(parseField(clazz, "timestamp", null), startDate, endDate);
            Where<EventRecord> fullWhere
                    = msgWhere
                    .and(emoWhere)
                    .and(roomWhere)
                    .and(srcWhere)
                    .and(tgtWhere)
                    .and(typeWhere)
                    .and(dateWhere);
            return fullWhere.doQuery(sortBy, startIndex, maxRecords);
        }
    }

    private List<EventRecord> searchEvents(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] roomNames = splitQuoted(req.getParameter("roomNames"));
        String[] sourceNames = splitQuoted(req.getParameter("sourcenames"));
        String[] targetNames = splitQuoted(req.getParameter("targetnames"));
        String[] messageTypes = req.getParameterValues("types");
        Long startDate = parseDate(req.getParameter("startdate"), 1L);
        Long endDate = parseDate(req.getParameter("enddate"), System.currentTimeMillis());
        Field sortBy = parseField(EventRecord.class, req.getParameter("sortby"), "timestamp");
        int startIndex = parseInt(req.getParameter("start"), 0, Integer.MAX_VALUE, 0);
        int maxRecords = 1000;
        Class<EventRecord> clazz = EventRecord.class;

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<EventRecord> searcher = daoConn.getEventDao().searcher();
            Where<EventRecord> roomWhere = whereMatchesAny(searcher, clazz, "room", roomNames);
            Where<EventRecord> srcWhere = whereMatchesAny(searcher, clazz, "sourceName", sourceNames);
            Where<EventRecord> tgtWhere = whereMatchesAny(searcher, clazz, "targetName", targetNames);
            Where<EventRecord> typeWhere = whereMatchesAny(searcher, clazz, "type", messageTypes);
            Where<EventRecord> dateWhere = searcher.whereBtw(parseField(clazz, "timestamp", null), startDate, endDate);
            Field typeFld = parseField(EventRecord.class, "type", null);
            Where<EventRecord> noMsgWhere = searcher.whereNeq(typeFld, "message").and(searcher.whereNeq(typeFld, "whisper"));
            Where<EventRecord> fullWhere
                    = roomWhere
                    .and(srcWhere)
                    .and(tgtWhere)
                    .and(typeWhere)
                    .and(dateWhere)
                    .and(noMsgWhere);
            return fullWhere.doQuery(sortBy, startIndex, maxRecords);
        }
    }

    private List<UserRecord> searchUsers(HttpServletRequest req, HttpServletResponse resp) throws DaoException {
        String[] usernames = splitQuoted(req.getParameter("targetnames"));
        String[] ownerNames = splitQuoted(req.getParameter("targetnames"));
        String[] acctTypes = req.getParameterValues("types");
        Long startDate = parseDate(req.getParameter("startdate"), 1L);
        Long endDate = parseDate(req.getParameter("enddate"), System.currentTimeMillis());
        Field sortBy = parseField(EventRecord.class, req.getParameter("sortby"), "timestamp");
        int startIndex = parseInt(req.getParameter("start"), 0, Integer.MAX_VALUE, 0);
        int maxRecords = 1000;
        Class<UserRecord> clazz = UserRecord.class;

        try (DaoConnection daoConn = daoFactory.openDaoConnection()) {
            Searcher<UserRecord> searcher = daoConn.getUserDao().searcher();
            Where<UserRecord> userWhere = whereMatchesAny(searcher, clazz, "username", usernames);
            Where<UserRecord> ownerWhere = whereMatchesAny(searcher, clazz, "ownerName", ownerNames);
            Where<UserRecord> typeWhere = whereMatchesAny(searcher, clazz, "type", acctTypes);
            Where<UserRecord> dateWhere = searcher.whereBtw(parseField(clazz, "timestamp", null), startDate, endDate);
            Where<UserRecord> fullWhere
                    = userWhere
                    .and(ownerWhere)
                    .and(typeWhere)
                    .and(dateWhere);
            return fullWhere.doQuery(sortBy, startIndex, maxRecords);
        }
    }

    //TODO optimize and, finish doquery, update MySQLUserDao
}
