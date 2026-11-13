/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hr.algebra.dal.sql;

import hr.algebra.dal.Repository;
import hr.algebra.model.Article;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class SqlRepository implements Repository {

    private static final String ID_ARTICLE    = "IDArticle";
    private static final String ID_CATEGORY   = "IDCategory";
    private static final String ID_AUTHOR     = "IDAuthor";
    private static final String TITLE         = "Title";
    private static final String LINK          = "Link";
    private static final String DESCRIPTION   = "Description";
    private static final String PICTURE_PATH  = "PicturePath";
    private static final String PUBLISHED_DATE = "PublishedDate";

    private static final String CREATE_ARTICLE      = "{ CALL createArticle (?,?,?,?,?,?) }";
    private static final String UPDATE_ARTICLE = "{ CALL updateArticle (?,?,?,?,?,?,?) }";
    private static final String DELETE_ARTICLE      = "{ CALL deleteArticle (?) }";
    private static final String DELETE_ALL_ARTICLES = "{ CALL deleteAllArticles }";
    private static final String ASSIGN_AUTHOR       = "{ CALL assignArticleAuthor (?,?) }";
    private static final String SELECT_ARTICLE      = "{ CALL selectArticle (?) }";
    private static final String SELECT_ARTICLES     = "{ CALL selectArticles }";

    @Override
    public int createArticle(Article article) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try(Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CREATE_ARTICLE)) {
            stmt.setString(TITLE, article.getTitle());
            stmt.setString(LINK, article.getLink());
            stmt.setString(DESCRIPTION, article.getDescription());
            stmt.setString(PICTURE_PATH, article.getPicturePath());
            stmt.setString(PUBLISHED_DATE, article.getPublishedDate()
                    .format(Article.DATE_FORMATTER));
          
            stmt.registerOutParameter(ID_ARTICLE, Types.INTEGER);
            
            stmt.executeUpdate();
            
            return stmt.getInt(ID_ARTICLE);
        }
    }

    @Override
    public void createArticles(List<Article> articles) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try(Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(CREATE_ARTICLE)) {
            
            for (Article article : articles) {
                stmt.setString(TITLE, article.getTitle());
                stmt.setString(LINK, article.getLink());
                stmt.setString(DESCRIPTION, article.getDescription());
                stmt.setString(PICTURE_PATH, article.getPicturePath());
                stmt.setString(PUBLISHED_DATE, article.getPublishedDate()
                        .format(Article.DATE_FORMATTER));
                
                stmt.registerOutParameter(ID_ARTICLE, Types.INTEGER);
                
                stmt.executeUpdate();
            }
            
        }
    }

    @Override
    public void updateArticle(int id, Article article) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try(Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(UPDATE_ARTICLE)) {
            stmt.setString(TITLE, article.getTitle());
            stmt.setString(LINK, article.getLink());
            stmt.setString(DESCRIPTION, article.getDescription());
            stmt.setString(PICTURE_PATH, article.getPicturePath());
            stmt.setString(PUBLISHED_DATE, article.getPublishedDate()
                    .format(Article.DATE_FORMATTER));
            stmt.setInt("IDCategory", article.getIdCategory());
          
            stmt.setInt(ID_ARTICLE, id);
            
            stmt.executeUpdate();
            
        }
    }

    @Override
    public void deleteArticle(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(DELETE_ARTICLE)) {
            stmt.setInt(ID_ARTICLE, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteAllArticles() throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(DELETE_ALL_ARTICLES)) {
            stmt.executeUpdate();
        }
    }

    @Override
    public void assignAuthor(int articleId, int authorId) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try (Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(ASSIGN_AUTHOR)) {
            stmt.setInt(ID_ARTICLE, articleId);
            stmt.setInt(ID_AUTHOR, authorId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Article> selectArticle(int id) throws Exception {
        DataSource dataSource = DataSourceSingleton.getInstance();
        try(Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_ARTICLE)) {
            stmt.setInt(ID_ARTICLE, id);
            
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Article a = new Article(
                            rs.getInt(ID_ARTICLE),
                            rs.getString(TITLE),
                            rs.getString(LINK),
                            rs.getString(DESCRIPTION),
                            rs.getString(PICTURE_PATH),
                            LocalDateTime.parse(rs.getString(PUBLISHED_DATE), Article.DATE_FORMATTER));
                    a.setIdCategory(rs.getInt(ID_CATEGORY));
                    a.setIdAuthor(rs.getInt(ID_AUTHOR));
                    return Optional.of(a);
                }
            }
            
        }
        return Optional.empty();
    }

    @Override
    public List<Article> selectArticles() throws Exception {
        List<Article> articles = new ArrayList<>();
       
        DataSource dataSource = DataSourceSingleton.getInstance();
        try(Connection con = dataSource.getConnection();
                CallableStatement stmt = con.prepareCall(SELECT_ARTICLES);
                ResultSet rs = stmt.executeQuery()) {
            
                while (rs.next()) {
                    Article a = new Article(
                            rs.getInt(ID_ARTICLE),
                            rs.getString(TITLE),
                            rs.getString(LINK),
                            rs.getString(DESCRIPTION),
                            rs.getString(PICTURE_PATH),
                            LocalDateTime.parse(rs.getString(PUBLISHED_DATE), Article.DATE_FORMATTER));
                    a.setIdCategory(rs.getInt(ID_CATEGORY));
                    a.setIdAuthor(rs.getInt(ID_AUTHOR));
                    articles.add(a);
                }
            }
            
         
        
        
        
        return articles;
    }

}
