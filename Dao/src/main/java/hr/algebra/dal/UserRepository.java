package hr.algebra.dal;

import hr.algebra.model.Role;
import hr.algebra.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void createUser(User user) throws Exception;

    Optional<User> selectUser(String username, String password) throws Exception;

    List<User> selectAllUsers() throws Exception;

    boolean userExists(String username) throws Exception;

    // Convenience default — no SP call needed, purely derived from the entity state
    default boolean isAdmin(User user) {
        return user.getRole() == Role.ADMINISTRATOR;
    }
}
