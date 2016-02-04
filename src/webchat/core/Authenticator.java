package webchat.core;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import webchat.dao.DaoConnection;
import webchat.dao.DaoConnectionFactory;
import webchat.dao.DaoException;
import webchat.dao.dto.AccountRecord;
import webchat.dao.dto.AdminRecord;
import webchat.dao.dto.UserRecord;
import webchat.dao.sql.MySQLDaoConnectionFactory;
import webchat.util.PasswordHash;

/**
 * Authenticates user and admin credentials against records in the database
 *
 * @author Nick
 *
 */
public class Authenticator {

    private DaoConnectionFactory daoFactory;

    public Authenticator(DaoConnectionFactory daoFactory) {
        super();
        this.daoFactory = daoFactory;
    }

    /**
     * Return the UserRecord for the account if the credentials are valid, or
     * null otherwise
     *
     * @param username
     * @param password
     * @return
     * @throws DaoException
     */
    public UserRecord authenticateUser(String username, String password) throws DaoException {
        try (DaoConnection dc = daoFactory.openDaoConnection();) {
            UserRecord usr = dc.getUserDao().find(username);
            return authenticate(usr, username, password);
        }
    }

    public UserRecord authenticateAgent(String username, String password, String uuid) throws DaoException {
        UserRecord usr = authenticateUser(username, password);
        if (usr != null) {
            if (uuid != null && uuid.equals(usr.getUuid())) {
                return usr;
            }
        }
        return null;
    }

    /**
     * Return the UserRecord for the account if the credentials are valid, or
     * null otherwise
     *
     * @param username
     * @param password
     * @return
     * @throws DaoException
     */
    public AdminRecord authenticateAdmin(String username, String password) throws DaoException {
        try (DaoConnection dc = daoFactory.openDaoConnection();) {
            AdminRecord usr = dc.getAdminDao().find(username);
            return authenticate(usr, username, password);
        }
    }


    /*
	 * Check if credentials valid by comparing them to the databse record
     */
    private <T extends AccountRecord> T authenticate(T usr, String username, String password) {

        try {
            //No record found, meaning username is invalid. Return null to indicate failed auth
            if (usr == null) {
                return null;
            }

            //Compute the hash of specified password and compare it to the known hash from the database
            String passHash = usr.getPasshash();
            boolean correctPassword = PasswordHash.validatePassword(password, passHash);

            //Does hash of specified password match the hash in the record?
            if (!correctPassword) {
                //No, return null to indicate auth failure
                return null;
            } else {
                //Yes, return record object to indicate auth success
                return usr;
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //Nothing we can do about this, throw a RuntimeException
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        DaoConnectionFactory df = MySQLDaoConnectionFactory.getInstance();
        Authenticator auth = new Authenticator(df);
        System.out.println(auth.authenticateUser("YoYoMa", "YoYoMa"));
        System.out.println(auth.authenticateUser("yoyoma", "YoYoMa"));
        System.out.println(auth.authenticateUser("YoYoMa", "yoyoma"));
        System.out.println(auth.authenticateUser("yoyoma", "yoyoMa"));
    }
}
