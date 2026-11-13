package hr.algebra.utilities;

import hr.algebra.model.Article;
import hr.algebra.model.RssTag;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RssParser {

    private static final String ASSETS_DIR = "assets";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS    = 15_000;

    private RssParser() {}

    public static List<Article> parse(String rssUrl) throws RssParseException {
        try {
            HttpURLConnection connection = openConnection(rssUrl);
            Document document;
            try (InputStream in = connection.getInputStream()) {
                document = parseXml(in);
            }
            connection.disconnect();
            return extractArticles(document);
        } catch (RssParseException e) {
            throw e;
        } catch (Exception e) {
            throw new RssParseException("Failed to parse RSS feed: " + rssUrl, e);
        }
    }

    private static HttpURLConnection openConnection(String url) throws RssParseException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (compatible; RssParser/1.0)");
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new RssParseException("HTTP " + status + " received from: " + url);
            }
            return connection;
        } catch (RssParseException e) {
            throw e;
        } catch (Exception e) {
            throw new RssParseException("Could not connect to: " + url, e);
        }
    }


    private static Document parseXml(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    private static List<Article> extractArticles(Document document) {
        List<Article> articles = new ArrayList<>();
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            try {
                articles.add(extractArticle(item));
            } catch (Exception ignored) {
            }
        }
        return articles;
    }

    private static Article extractArticle(Element item) throws Exception {
        String title       = firstTagText(item, RssTag.TITLE.getTagName());
        String link        = firstTagText(item, RssTag.LINK.getTagName());
        String description = firstTagText(item, RssTag.DESCRIPTION.getTagName());
        String pubDateStr  = firstTagText(item, RssTag.PUB_DATE.getTagName());

        if (title.isEmpty() || link.isEmpty()) {
            throw new Exception("Item missing required title or link — skipped");
        }

        LocalDateTime publishedDate = parseDate(pubDateStr);
        String picturePath          = resolveImagePath(item);

        return new Article(title, link, description, picturePath, publishedDate);
    }


    private static String resolveImagePath(Element item) {
        NodeList enclosures = item.getElementsByTagName(RssTag.ENCLOSURE.getTagName());
        for (int i = 0; i < enclosures.getLength(); i++) {
            Element enc  = (Element) enclosures.item(i);
            String type  = enc.getAttribute("type");
            String imgUrl = enc.getAttribute(RssTag.IMAGE_URL.getTagName());
            if (type.startsWith("image/") && !imgUrl.isEmpty()) {
                return downloadImage(imgUrl);
            }
        }

        NodeList mediaContent = item.getElementsByTagName("media:content");
        for (int i = 0; i < mediaContent.getLength(); i++) {
            Element mc     = (Element) mediaContent.item(i);
            String medium  = mc.getAttribute("medium");
            String type    = mc.getAttribute("type");
            String imgUrl  = mc.getAttribute(RssTag.IMAGE_URL.getTagName());
            boolean isImage = "image".equals(medium) || type.startsWith("image/");
            if (isImage && !imgUrl.isEmpty()) {
                return downloadImage(imgUrl);
            }
        }

        NodeList urlTags = item.getElementsByTagName(RssTag.IMAGE_URL.getTagName());
        if (urlTags.getLength() > 0) {
            String imgUrl = urlTags.item(0).getTextContent().trim();
            if (!imgUrl.isEmpty()) {
                return downloadImage(imgUrl);
            }
        }
NodeList encodedContent = item.getElementsByTagName("content:encoded");
if (encodedContent.getLength() > 0) {
    String html = encodedContent.item(0).getTextContent();
    int srcIdx = html.indexOf("src=\"http");
    while (srcIdx != -1) {
        int start = srcIdx + 5; 
        int end = html.indexOf("\"", start);
        if (end != -1) {
            String imgUrl = html.substring(start, end);
            if (imgUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$")) {
                return downloadImage(imgUrl);
            }
        }
        srcIdx = html.indexOf("src=\"http", srcIdx + 1);
    }
}
        return null; 
    }

    private static String downloadImage(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; RssParser/1.0)");

            String contentType = connection.getContentType();
            String ext = extensionFromContentType(contentType);
            if (ext == null) {
                ext = extensionFromUrl(imageUrl);
            }

            String filename  = UUID.randomUUID().toString() + ext;
            String localPath = ASSETS_DIR + "/" + filename;

            Files.createDirectories(Paths.get(ASSETS_DIR));

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, Paths.get(localPath));
            }
            connection.disconnect();
            return localPath;

        } catch (Exception e) {
            Logger.getLogger(RssParser.class.getName()).log(Level.WARNING,
                    "Could not download image: " + imageUrl, e);
            return null;
        }
    }



    private static String firstTagText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return ZonedDateTime.parse(dateStr.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                                .toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private static String extensionFromContentType(String contentType) {
        if (contentType == null) return null;
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
        if (contentType.contains("png"))  return ".png";
        if (contentType.contains("gif"))  return ".gif";
        if (contentType.contains("webp")) return ".webp";
        return null;
    }

    private static String extensionFromUrl(String url) {
        int dot   = url.lastIndexOf('.');
        int slash = url.lastIndexOf('/');
        if (dot > slash && dot != -1) {
            String ext = url.substring(dot);
            int query  = ext.indexOf('?');
            if (query != -1) ext = ext.substring(0, query);
            return (ext.length() >= 2 && ext.length() <= 5) ? ext : ".jpg";
        }
        return ".jpg";
    }
}
