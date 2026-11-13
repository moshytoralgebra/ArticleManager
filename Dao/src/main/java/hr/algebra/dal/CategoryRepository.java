package hr.algebra.dal;

import hr.algebra.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    int createCategory(Category category) throws Exception;

    void updateCategory(int id, Category category) throws Exception;

    void deleteCategory(int id) throws Exception;

    Optional<Category> selectCategory(int id) throws Exception;

    List<Category> selectAllCategories() throws Exception;

    default boolean hasName(Category c, String name) {
        return c.getName().equalsIgnoreCase(name);
    }
}
