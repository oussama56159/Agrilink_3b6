package services;
import models.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService extends IService<User> {
    // Additional methods specific to User login
    User login(String email, String password) throws Exception;
    boolean verifyCredentials(String email, String password) throws Exception;
    User findByEmail(String email);
    void add(User user);
    void update(User user) throws Exception;
    void delete(User user) throws Exception;
    int[] getUserTypeStatistics() throws SQLException;
    void updatePassword(String email, String newPassword) throws Exception;
}