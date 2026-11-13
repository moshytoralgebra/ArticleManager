package hr.algebra.dal.sql;

import hr.algebra.dal.CategoryRepository;
import hr.algebra.model.Category;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class SqlCategoryRepository implements CategoryRepository {

    // Column name constants — must match Category table column names exactly
    private static final String ID_CATEGORY  = "IDCategory";
    private static final String NAME         = "Name";
    private static final String DESCRIPTION  = "Description";

    // CALL strings — positional '?' count matches SP parameter count
    private static final String CREATE_CATEGORY       = "{ CALL createCategory (?,?,?) }";
    private static final String UPDATE_CATEGORY       = "{ CALL updateCategory (?,?,?) }";
    private static final String DELETE_CATEGORY       = "{ CALL deleteCategory (?) }";
    private static final String SELECT_CATEGORY       = "{ CALL selectCategory (?) }";
    private static final String SELECT_ALL_CATEGORIES = "{ CALL selectAllCategories }";

    @Override
    public int createCategory(Category category) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CREATE_CATEGORY)) {

            stmt.setString(NAME,        category.getName());
            stmt.setString(DESCRIPTION, category.getDescription());
            stmt.registerOutParameter(ID_CATEGORY, Types.INTEGER);

            stmt.executeUpdate();
            return stmt.getInt(ID_CATEGORY);
        }
    }

    @Override
    public void updateCategory(int id, Category category) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(UPDATE_CATEGORY)) {

            stmt.setString(NAME,        category.getName());
            stmt.setString(DESCRIPTION, category.getDescription());
            stmt.setInt(ID_CATEGORY, id);

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteCategory(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(DELETE_CATEGORY)) {

            stmt.setInt(ID_CATEGORY, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Category> selectCategory(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_CATEGORY)) {

            stmt.setInt(ID_CATEGORY, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Category> selectAllCategories() throws Exception {
        List<Category> categories = new ArrayList<>();

        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_ALL_CATEGORIES);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        }
        return categories;
    }

    private Category mapRow(ResultSet rs) throws Exception {
        return new Category(
                rs.getInt(ID_CATEGORY),
                rs.getString(NAME),
                rs.getString(DESCRIPTION)
        );
    }
}
