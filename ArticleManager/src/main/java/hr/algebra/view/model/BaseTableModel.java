package hr.algebra.view.model;

import java.util.List;
import javax.swing.table.AbstractTableModel;

public abstract class BaseTableModel<T> extends AbstractTableModel {

    protected List<T> items;

    protected BaseTableModel(List<T> items) {
        this.items = items;
    }

    public void setItems(List<T> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public abstract String[] getColumnNames();

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public String getColumnName(int col) {
        return getColumnNames()[col];
    }
}
