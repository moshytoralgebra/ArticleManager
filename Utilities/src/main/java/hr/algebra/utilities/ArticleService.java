package hr.algebra.utilities;

import hr.algebra.model.Article;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ArticleService {

    private ArticleService() {}

    public static List<Article> filterByKeyword(List<Article> articles, Predicate<Article> predicate) {
        return articles.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static List<String> extractField(List<Article> articles, Function<Article, String> extractor) {
        return articles.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

    public static void forEach(List<Article> articles, Consumer<Article> action) {
        articles.stream().forEach(action);
    }

    public static Optional<Article> findFirstSorted(List<Article> articles, int skip) {
        Set<String> seen = new HashSet<>();
        return articles.stream()
                .sorted(Comparator.comparing(Article::getTitle))
                .filter(a -> seen.add(a.getTitle()))  
                .skip(skip)
                .findFirst();
    }

    public static Optional<Article> findShortest(List<Article> articles) {
        return articles.stream()
                .min(Comparator.comparingInt(a -> a.getTitle().length()));
    }

    public static Optional<Article> findLongest(List<Article> articles) {
        return articles.stream()
                .max(Comparator.comparingInt(a -> a.getTitle().length()));
    }

    public static boolean anyMatch(List<Article> articles, Predicate<Article> predicate) {
        return articles.stream().anyMatch(predicate);
    }

    public static boolean noneMatch(List<Article> articles, Predicate<Article> predicate) {
        return articles.stream().noneMatch(predicate);
    }

    public static Map<String, List<Article>> groupByAuthor(List<Article> articles) {
        return articles.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAuthor() != null ? a.getAuthor().getName() : "Unknown"));
    }

    public static List<Article> filterAndCollect(List<Article> articles, Predicate<Article> predicate) {
        return articles.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static List<Article> distinctById(List<Article> articles) {
        return articles.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public static Optional<Article> findAny(List<Article> articles, Predicate<Article> predicate) {
        return articles.stream()
                .filter(predicate)
                .findAny();
    }
}
