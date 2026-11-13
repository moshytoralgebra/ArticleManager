/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hr.algebra.dal;

import hr.algebra.model.Article;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author daniel.bele
 */
public interface Repository {
    
    int createArticle(Article article) throws Exception;
    void createArticles(List<Article> articles) throws Exception;
    void updateArticle(int id, Article article) throws Exception;
    void deleteArticle(int id) throws Exception;
    void deleteAllArticles() throws Exception;
    void assignAuthor(int articleId, int authorId) throws Exception;
    Optional<Article> selectArticle(int id) throws Exception;
    List<Article> selectArticles() throws Exception;

    default boolean exists(int id) {
        try {
            return selectArticle(id).isPresent();
        } catch (Exception ex) {
            return false;
        }
    }
}
