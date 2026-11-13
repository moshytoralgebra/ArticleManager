package hr.algebra.dal.sql;

import hr.algebra.dal.AuthorRepository;
import hr.algebra.model.Author;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class SqlAuthorRepository implements AuthorRepository {

    // Column name constants — must match Author table column names exactly
    private static final String ID_AUTHOR = "IDAuthor";
    private static final String NAME      = "Name";
    private static final String BIO       = "Bio";
    private static final String EMAIL     = "Email";

    // CALL strings — positional '?' count matches SP parameter count
    private static final String CREATE_AUTHOR      = "{ CALL createAuthor (?,?,?,?) }";
    private static final String UPDATE_AUTHOR      = "{ CALL updateAuthor (?,?,?,?) }";
    private static final String DELETE_AUTHOR      = "{ CALL deleteAuthor (?) }";
    private static final String SELECT_AUTHOR      = "{ CALL selectAuthor (?) }";
    private static final String SELECT_ALL_AUTHORS = "{ CALL selectAllAuthors }";

    @Override
    public int createAuthor(Author author) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CREATE_AUTHOR)) {

            stmt.setString(NAME,  author.getName());
            stmt.setString(BIO,   author.getBio());
            stmt.setString(EMAIL, author.getEmail());
            stmt.registerOutParameter(ID_AUTHOR, Types.INTEGER);

            stmt.executeUpdate();
            return stmt.getInt(ID_AUTHOR);
        }
    }

    @Override
    public void updateAuthor(int id, Author author) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(UPDATE_AUTHOR)) {

            stmt.setString(NAME,  author.getName());
            stmt.setString(BIO,   author.getBio());
            stmt.setString(EMAIL, author.getEmail());
            stmt.setInt(ID_AUTHOR, id);

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteAuthor(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(DELETE_AUTHOR)) {

            stmt.setInt(ID_AUTHOR, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Author> selectAuthor(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_AUTHOR)) {

            stmt.setInt(ID_AUTHOR, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Author> selectAllAuthors() throws Exception {
        List<Author> authors = new ArrayList<>();

        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_ALL_AUTHORS);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                authors.add(mapRow(rs));
            }
        }
        return authors;
    }

    private Author mapRow(ResultSet rs) throws Exception {
        return new Author(
                rs.getInt(ID_AUTHOR),
                rs.getString(NAME),
                rs.getString(BIO),
                rs.getString(EMAIL)
        );
    }
}
