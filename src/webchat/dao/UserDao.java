package webchat.dao;

import webchat.dao.dto.UserRecord;
import java.util.List;
import java.lang.reflect.Field;

public interface UserDao {

    void save(UserRecord user) throws DaoException;

    void modify(UserRecord user) throws DaoException;

    UserRecord find(String name) throws DaoException;

    Searcher<UserRecord> searcher();

    default UserRecord findActiveByName(String name) throws DaoException {
        try {
            Searcher<UserRecord> searcher = searcher();
            Field nameField = UserRecord.class.getDeclaredField("username");
            Field destroyedField = UserRecord.class.getDeclaredField("destroyed");
            Where<UserRecord> whereName = searcher.whereLwrEq(nameField, name);
            Where<UserRecord> whereDestroyed = searcher.whereNull(destroyedField);
            List<UserRecord> users = whereName.and(whereDestroyed).doQuery();
            if (users.isEmpty()) {
                return null;
            } else if (users.size() == 1) {
                return users.get(0);
            } else {
                throw new DaoException("Integrity exception: more than one active user with name " + name);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    default UserRecord findActiveByEmail(String email) throws DaoException {
        try {
            Searcher<UserRecord> searcher = searcher();
            Field emailField = UserRecord.class.getDeclaredField("email");
            Field destroyedField = UserRecord.class.getDeclaredField("destroyed");
            Where<UserRecord> whereEmail = searcher.whereLwrEq(emailField, email);
            Where<UserRecord> whereActive = searcher.whereNull(destroyedField);
            List<UserRecord> users = whereEmail.and(whereActive).doQuery();
            if (users.isEmpty()) {
                return null;
            } else if (users.size() == 1) {
                return users.get(0);
            } else {
                throw new DaoException("Integrity exception: more than one active user with email " + email);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    default UserRecord findById(int id) throws DaoException {
        try {
            Searcher<UserRecord> searcher = searcher();
            Field idField = UserRecord.class.getDeclaredField("id");
            Where<UserRecord> whereId = searcher.whereEq(idField, id);
            List<UserRecord> users = whereId.doQuery();
            if (users.isEmpty()) {
                return null;
            } else if (users.size() == 1) {
                return users.get(0);
            } else {
                throw new DaoException("Integrity exception: more than one user with id " + id);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    
}
