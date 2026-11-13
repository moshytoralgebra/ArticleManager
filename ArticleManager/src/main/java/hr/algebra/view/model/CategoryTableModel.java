package hr.algebra.view.model;

import hr.algebra.model.Category;
import java.util.List;

public final class CategoryTableModel extends BaseTableModel<Category> {

    private static final String[] COLUMNS = {"Id", "Name", "Description"};

    public CategoryTableModel(List<Category> categories) {
        super(categories);
    }

    public void setCategories(List<Category> categories) {
        setItems(categories);
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
        Category c = items.get(rowIndex);
        switch (columnIndex) {
            case 0: return c.getId();
            case 1: return c.getName();
            case 2: return c.getDescription();
        }
        throw new RuntimeException("No such column");
    }
}
