package hr.algebra.model;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(String value) {
        return LocalDateTime.parse(value, Article.DATE_FORMATTER);
    }

    @Override
    public String marshal(LocalDateTime value) {
        return value == null ? null : value.format(Article.DATE_FORMATTER);
    }
}
