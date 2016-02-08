package webchat.servlet;

import java.io.IOException;
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


public class WebChatServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private List<Path> emoticonPaths;
	
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
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
	

	protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String relPath = req.getServletPath().toLowerCase();
                if (relPath.equals("/")) {
                    req.getRequestDispatcher("main.jsp").forward(req, resp);
                } else if (relPath.startsWith("/room")) {
                    //forward processing to the chatroom jsp page
                    req.getRequestDispatcher("chatroom.jsp").forward(req, resp);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                
	}


}
