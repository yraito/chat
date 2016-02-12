package webchat.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
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
        urlPatterns = {"/rooms", "/chat", "/main", "/account"}
)
public class WebChatServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<String> emoticonPaths;

    @Override
    public void init(ServletConfig config) throws ServletException {
        Set<String> imagePaths = config.getServletContext().getResourcePaths("/emoticons/");

        if (imagePaths == null) {
            throw new ServletException("Can't find emoticons directory");

        }
        //Collect the filepaths of the emoticon images (all .png files in /images) into a List
        try (Stream<String> pathStream = imagePaths.stream()) {
            this.emoticonPaths = pathStream
                    .filter(p -> p.toLowerCase().endsWith(".png"))
                    .collect(Collectors.toList());
        }
        //Store the list in the application context, where it can be accessed by the
        //chatroom jsp page
        System.out.println("Paths:" + emoticonPaths);
        config.getServletContext().setAttribute("emoticonPaths", emoticonPaths);
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
        String relPath = req.getServletPath().toLowerCase();
        UserRecord userRecord = HttpServletClientSession.getUserRecord(req.getSession());
        if (userRecord == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
            return;
        }
        req.setAttribute("userRecord", userRecord);
        if (relPath.startsWith("/main")) {

            req.getRequestDispatcher("/WEB-INF/main.jsp").forward(req, resp);
        } else if (relPath.startsWith("/rooms")) {
            //forward processing to the chatroom jsp page
            ChatManager chatMgr = (ChatManager) req.getServletContext().getAttribute(ApiServletListener.CHAT_MANAGER_KEY);
            Collection<RoomInfo> roomInfos = chatMgr.listRoomInfos();
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
                Where<UserRecord> where = searcher.whereEq(ownerIdField, userId);
                List<UserRecord> agents = where.doQuery();
                req.setAttribute("agents", agents);
                req.getRequestDispatcher("/WEB-INF/accountstub.jsp").forward(req, resp);
            } catch (NoSuchFieldException e) {
                throw new MappingException(e);
            }

        }

    }

}
