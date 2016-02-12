/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.servlet.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import webchat.core.*;
import webchat.servlet.*;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.sql.MySQLDaoConnectionFactory;

/**
 *
 * @author Nick
 */
public class ApiServletListener implements ServletContextListener {

    public final static String CHAT_MANAGER_KEY = "chat_manager";

    public final static String DAO_FACTORY_KEY = "dao_factory";
    
    public final static String EMOTICONS_KEY = "emoticonPaths";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DaoConnectionFactory daoFactory = MySQLDaoConnectionFactory.getInstance();
        ChatManager chatManager = new ChatManager(daoFactory);
        sce.getServletContext().setAttribute(CHAT_MANAGER_KEY, chatManager);
        sce.getServletContext().setAttribute(DAO_FACTORY_KEY, daoFactory);
        loadEmoticons(sce.getServletContext());
    }

    private void loadEmoticons(ServletContext sc) {
        Set<String> imagePaths = sc.getResourcePaths("/emoticons/");
        if (imagePaths == null) {
            throw new ConfigurationException("Can't find emoticons directory");

        }
        //Collect the filepaths of the emoticon images (all .png files in /images) into a List
        try (Stream<String> pathStream = imagePaths.stream()) {
            List<Emoticon> emoticonPaths = pathStream
                    .filter(p -> p.toLowerCase().endsWith(".png"))
                    .map(s->new Emoticon("[" + s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.')) + "]", s))
                    .collect(Collectors.toList());
            //Store the list in the application context, where it can be accessed by the
            //chatroom jsp page
            System.out.println("Paths:" + emoticonPaths);
            sc.setAttribute(EMOTICONS_KEY, emoticonPaths);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //
    }

}
