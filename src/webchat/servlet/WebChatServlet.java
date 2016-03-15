package webchat.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.lang.reflect.Field;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import webchat.servlet.api.ApiServletListener;

import webchat.core.*;
import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.Searcher;
import webchat.dao.Where;
import webchat.dao.dto.UserRecord;
import webchat.dao.sql.MappingException;
import webchat.servlet.api.HttpServletClientSession;

@WebServlet(
        name = "WebChatServlet",
        urlPatterns = {"/web/rooms", "/web/chat", "/web/main", "/web/account", "/web/login", "/web/register", "/web", "/web/"}
)
public class WebChatServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<String> emoticonPaths;

   
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String relPath = req.getServletPath().substring(4).toLowerCase();
        UserRecord userRecord = HttpServletClientSession.getUserRecord(req.getSession());
        if (userRecord == null &&!relPath.startsWith("/login") && !relPath.startsWith("/register")) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
            return;
        }
        req.setAttribute("userRecord", userRecord);
        if (relPath.startsWith("/login")) {
            req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
        } else if (relPath.startsWith("/register")) {
            req.getRequestDispatcher("/WEB-INF/register.jsp").forward(req, resp);
        } else if (relPath.startsWith("/main")) {
            req.getRequestDispatcher("/WEB-INF/main.jsp").forward(req, resp);
        } else if (relPath.startsWith("/rooms")) {
            //forward processing to the chatroom jsp page
            ChatManager chatMgr = (ChatManager) req.getServletContext().getAttribute(ApiServletListener.CHAT_MANAGER_KEY);
            Collection<RoomInfo> roomInfos = chatMgr.listRoomInfos();
            Set<String> joinedRooms = new HashSet<>();
            for (RoomBean r : chatMgr.getRooms()) {
                if (r.hasUser(userRecord.getUsername())) {
                    joinedRooms.add(r.getName());
                }
            }
            req.setAttribute("joinedRooms", joinedRooms);
            req.setAttribute("roomInfos", roomInfos);
            req.getRequestDispatcher("/WEB-INF/roomsstub.jsp").forward(req, resp);
        } else if (relPath.startsWith("/chat")) {
            req.getRequestDispatcher("/WEB-INF/chatstub.jsp").forward(req, resp);
        } else if (relPath.startsWith("/account")) {
            DaoConnectionFactory daoFactory = (DaoConnectionFactory) req.getServletContext().getAttribute(ApiServletListener.DAO_FACTORY_KEY);
            int userId = userRecord.getId();
            try (DaoConnection dc = daoFactory.openDaoConnection()) {
                Searcher<UserRecord> searcher = dc.getUserDao().searcher();
                Field ownerIdField = UserRecord.class.getDeclaredField("ownerId");
                Field destroyedField = UserRecord.class.getDeclaredField("destroyed");
                Where<UserRecord> whereOwner = searcher.whereEq(ownerIdField, userId);
                Where<UserRecord> whereActive = searcher.whereNull(destroyedField);
                Where<UserRecord> where = whereOwner.and(whereActive);
                List<UserRecord> agents = where.doQuery();
                req.setAttribute("agents", agents);
                req.getRequestDispatcher("/WEB-INF/accountstub.jsp").forward(req, resp);
            } catch (NoSuchFieldException e) {
                throw new MappingException(e);
            }

        } else if ( relPath.equals("") || relPath.equals("/")) {
            resp.sendRedirect("/web/main");
        }

    }

}
