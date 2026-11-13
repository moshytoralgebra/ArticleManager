package hr.algebra.model;

public enum RssTag {

    TITLE("title"),
    LINK("link"),
    DESCRIPTION("description"),
    PUB_DATE("pubDate"),
    ENCLOSURE("enclosure"),
    IMAGE_URL("url");   

    private final String tagName;

    RssTag(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }
}
