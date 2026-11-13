package hr.algebra.dal.sql;

import hr.algebra.dal.UserRepository;
import hr.algebra.model.Role;
import hr.algebra.model.User;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class SqlUserRepository implements UserRepository {

    // Column name constants — must match the Users table column names exactly
    private static final String ID_USER    = "IDUser";
    private static final String USERNAME   = "Username";
    private static final String PASSWORD   = "Password";
    private static final String ROLE       = "Role";

    // Output parameter name for checkUserExists
    private static final String USER_EXISTS = "UserExists";

    // CALL strings — positional '?' count must match SP parameter count
    private static final String CREATE_USER                = "{ CALL createUser (?,?,?) }";
    private static final String SELECT_USER_BY_CREDENTIALS = "{ CALL selectUserByCredentials (?,?) }";
    private static final String SELECT_ALL_USERS           = "{ CALL selectAllUsers }";
    private static final String CHECK_USER_EXISTS          = "{ CALL checkUserExists (?,?) }";

    @Override
    public void createUser(User user) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CREATE_USER)) {

            stmt.setString(USERNAME, user.getUsername());
            stmt.setString(PASSWORD, user.getPassword());
            stmt.setString(ROLE,     user.getRole().name());

            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<User> selectUser(String username, String password) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_USER_BY_CREDENTIALS)) {

            stmt.setString(USERNAME, username);
            stmt.setString(PASSWORD, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> selectAllUsers() throws Exception {
        List<User> users = new ArrayList<>();

        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_ALL_USERS);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    @Override
    public boolean userExists(String username) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CHECK_USER_EXISTS)) {

            stmt.setString(USERNAME,    username);
            stmt.registerOutParameter(USER_EXISTS, Types.BIT);

            stmt.executeUpdate();

            return stmt.getBoolean(USER_EXISTS);
        }
    }

    // Extracts a User from the current ResultSet row — avoids repeating the
    // same rs.get* calls in both selectUser and selectAllUsers.
    private User mapRow(ResultSet rs) throws Exception {
        return new User(
                rs.getInt(ID_USER),
                rs.getString(USERNAME),
                rs.getString(PASSWORD),
                Role.valueOf(rs.getString(ROLE))
        );
    }
}
