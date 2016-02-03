package webchat.dao.sql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import webchat.core.ConfigurationException;
import webchat.dao.*;
import webchat.dao.dto.UserRecord;
import webchat.util.PasswordHash;

public class MySQLDaoConnectionFactory implements DaoConnectionFactory {

    private final static String propFile = "jdbc.properties";

    private static MySQLDaoConnectionFactory instance;

    public synchronized static MySQLDaoConnectionFactory getInstance() {
        if (instance == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream propertiesIs = classLoader.getResourceAsStream(propFile);

            if (propertiesIs == null) {
                throw new ConfigurationException("Missing " + propFile + " file in classpath");
            }

            Properties properties = new Properties();
            try {
                properties.load(propertiesIs);
            } catch (IOException e) {
                throw new ConfigurationException("Error reading properties file " + propFile, e);
            }

            String url = properties.getProperty("url");
            String driver = properties.getProperty("driver");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");

            instance = new MySQLDaoConnectionFactory(url, driver, username, password);
        }

        return instance;
    }

    private String jdbdS;

    private MySQLDaoConnectionFactory(String url, String driver, String usr, String pass) {
        jdbdS = url + "?user=" + usr + "&password=" + pass;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public DaoConnection openDaoConnection() throws DaoException {
        try {
            return new MySQLDaoConnection(getConnection());
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbdS);
    }

    public static void main(String[] ss) throws Exception {
        MySQLDaoConnectionFactory df = MySQLDaoConnectionFactory.getInstance();
        try (DaoConnection dc = df.openDaoConnection()){
            UserDao ud = dc.getUserDao();
            String s = PasswordHash.createHash("yadayada");
            UserRecord ur = new UserRecord();
            ur.setUsername("YoYoMaAgent99");
            ur.setOwnerId(1);
            ur.setCreated(System.currentTimeMillis());
            ur.setEmail("amooyoy@ollec.com");
            ur.setPasshash(s);
            ud.save(ur);
            List l = ud.searcher().where1is1().doQuery();
            System.out.println(l);
        } 

    }
}
