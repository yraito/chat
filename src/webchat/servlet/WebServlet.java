package webchat.servlet;

import webchat.servlet.api.HttpServletClientSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class WebServlet extends HttpServlet {

	/**
	 * sign in, sign up, main, rooms, room, acct
	 */
	private static final long serialVersionUID = 1L;

	private List<Path> emoticonPaths;
	private DaoConnectionFactory daoFactory;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		//Find the /images directory containing the emoticon images, and make sure it exists
		Path imagesDir = Paths.get("images");
		if (!Files.isDirectory(imagesDir)) {
			throw new ServletException("Can't find emoticon img directory " + imagesDir.toAbsolutePath());
		}
		//Collect the filepaths of the emoticon images (all .png files in /images) into a List
		try (Stream<Path> pathStream = Files.list(imagesDir)) {
			this.emoticonPaths = pathStream
			.filter(p->p.getFileName().toString().toLowerCase().endsWith(".png"))
			.collect(Collectors.toList());
		} catch (IOException e) {
			throw new ServletException("Error loading emoticon paths", e);
		}
		//Store the list in the application context, where it can be accessed by the
		//chatroom jsp page
		config.getServletContext().setAttribute("emoticons", emoticonPaths);
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}

	
	protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	private String getMainPageView(HttpServletRequest req, HttpServletResponse resp) {
		return "main.jsp";
	}
	
	private String getRoomListView(HttpServletRequest req, HttpServletResponse resp) {
		return "rooms.jsp";
	}
	
	private String getChatInterfaceView(HttpServletRequest req, HttpServletResponse resp) {
		return "chatroom.jsp";
	}
	
	private String getAccountInfoView(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		
		String userName = (String) req.getSession().getAttribute(null);
		
		try(DaoConnection daoConn = daoFactory.openDaoConnection()) {
			
			UserRecord userRecord = daoConn.getUserDao().find(userName);
			if (userRecord == null) {
				throw new ServletException("Csn't find dao record for user " + userName);
			}

			Searcher<UserRecord> searcher = daoConn.getUserDao().searcher();
			Field ownerField = UserRecord.class.getDeclaredField("ownerId");
			Integer userId = userRecord.getId();
			Where<UserRecord> where = searcher.whereEq(ownerField, userId);
			List<UserRecord> agents = where.doQuery();
			
			req.setAttribute("userrecords", userRecord);
			req.setAttribute("agentrecords", agents);
			return "account.jsp";
		} catch (DaoException | NoSuchFieldException | SecurityException e) {
			throw new ServletException(e);
		}
	}
	
	
}
