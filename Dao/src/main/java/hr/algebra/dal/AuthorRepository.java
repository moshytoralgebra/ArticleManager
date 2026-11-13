package hr.algebra.dal;

import hr.algebra.model.Author;
import java.util.List;
import java.util.Optional;

public interface AuthorRepository {

    int createAuthor(Author author) throws Exception;

    void updateAuthor(int id, Author author) throws Exception;

    void deleteAuthor(int id) throws Exception;

    Optional<Author> selectAuthor(int id) throws Exception;

    List<Author> selectAllAuthors() throws Exception;

    default String getDisplayName(Author a) {
        return a.getName() + " <" + a.getEmail() + ">";
    }
}
