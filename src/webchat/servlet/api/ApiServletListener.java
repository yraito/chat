/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.servlet.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import webchat.core.ChatManager;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.sql.MySQLDaoConnectionFactory;
/**
 *
 * @author Nick
 */
public class ApiServletListener implements ServletContextListener {

    public final static String CHAT_MANAGER_KEY = "chat_manager";
    
    public final static String DAO_FACTORY_KEY = "dao_factory";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DaoConnectionFactory daoFactory = MySQLDaoConnectionFactory.getInstance();
        ChatManager chatManager = new ChatManager(daoFactory);
        sce.getServletContext().setAttribute(CHAT_MANAGER_KEY, chatManager);
        sce.getServletContext().setAttribute(DAO_FACTORY_KEY, daoFactory);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //
    }
    
    
}
