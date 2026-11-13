package hr.algebra.view.model;

import hr.algebra.model.Author;
import java.util.List;

public final class AuthorTableModel extends BaseTableModel<Author> {

    private static final String[] COLUMNS = {"Id", "Name", "Bio", "Email"};

    public AuthorTableModel(List<Author> authors) {
        super(authors);
    }

    public void setAuthors(List<Author> authors) {
        setItems(authors);
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
        Author a = items.get(rowIndex);
        switch (columnIndex) {
            case 0: return a.getId();
            case 1: return a.getName();
            case 2: return a.getBio();
            case 3: return a.getEmail();
        }
        throw new RuntimeException("No such column");
    }
}
