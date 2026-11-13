package hr.algebra.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "articles")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArticleList {

    @XmlElement(name = "article")
    private List<Article> articles;

    public ArticleList() {}

    public ArticleList(List<Article> articles) {
        this.articles = articles;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
