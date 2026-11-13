package hr.algebra.view.model;

import hr.algebra.model.Article;
import java.util.List;

public final class ArticleTableModel extends BaseTableModel<Article> {

    private static final String[] COLUMNS = {
        "Id", "Title", "Link", "Description", "Picture path", "Published date"
    };

    public ArticleTableModel(List<Article> articles) {
        super(articles);
    }

    public void setArticles(List<Article> articles) {
        setItems(articles);
    }

    @Override
    public String[] getColumnNames() {
        return COLUMNS;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Integer.class;
        return super.getColumnClass(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Article a = items.get(rowIndex);
        switch (columnIndex) {
            case 0: return a.getId();
            case 1: return a.getTitle();
            case 2: return a.getLink();
            case 3: return a.getDescription();
            case 4: return a.getPicturePath();
            case 5: return a.getPublishedDate().format(Article.DATE_FORMATTER);
        }
        throw new RuntimeException("No such column");
    }
}
