/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hr.algebra.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author daniel.bele
 */
@XmlRootElement(name = "article")
@XmlAccessorType(XmlAccessType.NONE)
public final class Article extends BaseEntity implements Comparable<Article> {

    //2011-12-03T10:15:30
    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // id field lives in BaseEntity; expose it to JAXB via an annotated override
    @Override
    @XmlElement(name = "id")
    public int getId() { return super.getId(); }

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "link")
    private String link;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "picturePath")
    private String picturePath;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlElement(name = "publishedDate")
    private LocalDateTime publishedDate;

    @XmlElement(name = "idCategory")
    private int       idCategory;

    @XmlElement(name = "idAuthor")
    private int       idAuthor;

    // category and author are not serialised to XML — excluded under XmlAccessType.NONE
    private Category  category;
    private Author    author;

    public Article() {
    }
    
    

    public Article(int id, String title, String link, String description, String picturePath, LocalDateTime publishedDate) {
        this(title, link, description, picturePath, publishedDate);
        setId(id);
    }

    public Article(String title, String link, String description, String picturePath, LocalDateTime publishedDate) {
        setTitle(title);
        setLink(link);
        setDescription(description);
        setPicturePath(picturePath);
        setPublishedDate(publishedDate);
    }

    @Override
    public String getDisplayName() { return title; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public int getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(int idAuthor) {
        this.idAuthor = idAuthor;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + getId();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return getId() == ((Article) obj).getId();
    }

    @Override
    public int compareTo(Article o) {
        return Integer.compare(getId(), o.getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getId() + " - " + title);
        if (category != null) sb.append(" [").append(category.getName()).append("]");
        if (author != null)   sb.append(" by ").append(author.getName());
        return sb.toString();
    }
    
    
    
    
    
}
