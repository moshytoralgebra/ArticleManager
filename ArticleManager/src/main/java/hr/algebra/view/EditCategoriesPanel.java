package hr.algebra.view;

import hr.algebra.dal.CategoryRepository;
import hr.algebra.dal.sql.SqlCategoryRepository;
import hr.algebra.model.Category;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.view.model.CategoryTableModel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

public class EditCategoriesPanel extends javax.swing.JPanel {

    public EditCategoriesPanel() {
        initComponents();
        init();
    }

    private void initComponents() {
        tfName        = new JTextField(30);
        taDescription = new JTextArea(4, 30);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        lbNameError   = createErrorLabel();
        btnAdd        = new JButton("Add");
        btnUpdate     = new JButton("Update");
        btnDelete     = new JButton("Delete");
        btnDelete.setBackground(new Color(204, 0, 51));
        btnDelete.setForeground(Color.WHITE);
        tbCategories  = new javax.swing.JTable();

        btnAdd.addActionListener(this::btnAddActionPerformed);
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        tbCategories.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectEntity(); }
        });
        tbCategories.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { selectEntity(); }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildFormPanel(),
                new JScrollPane(tbCategories));
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(195);

        setLayout(new java.awt.BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(splitPane, java.awt.BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);

        // row 0 — Name
        gbc.gridy = 0; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(tfName, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(lbNameError, gbc);

        // row 1 — Description (optional, no error label)
        gbc.gridy = 1; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        form.add(new JScrollPane(taDescription), gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel(""), gbc); // spacer keeps grid aligned

        // row 2 — buttons
        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 8, 5, 8);
        form.add(btnPanel, gbc);

        return form;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("X");
        lbl.setForeground(new Color(204, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    private void init() {
        initValidation();
        hideErrors();
        initTable();
    }

    private void initValidation() {
        validationFields = Arrays.asList(tfName);
        errorLabels      = Arrays.asList(lbNameError);
    }

    private void initTable() {
        tbCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbCategories.setAutoCreateRowSorter(true);
        tbCategories.setRowHeight(25);
        model = new CategoryTableModel(new ArrayList<>());
        tbCategories.setModel(model);

        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return repository.selectAllCategories();
            }

            @Override
            protected void done() {
                try {
                    model.setCategories(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    MessageUtils.showErrorMessage("Load Error", "Cannot load categories: " +
                            (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    private void hideErrors() {
        errorLabels.forEach(e -> e.setVisible(false));
    }

    private boolean formValid() {
        hideErrors();
        boolean ok = true;
        for (int i = 0; i < validationFields.size(); i++) {
            boolean empty = validationFields.get(i).getText().trim().isEmpty();
            ok &= !empty;
            errorLabels.get(i).setVisible(empty);
        }
        return ok;
    }

    private void clearForm() {
        hideErrors();
        validationFields.forEach(e -> e.setText(""));
        taDescription.setText("");
        selectedCategory = null;
    }

    // ---------------------------------------------------------------
    // Table selection
    // ---------------------------------------------------------------

    private void selectEntity() {
        clearForm();
        int row = tbCategories.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        try {
            // ifPresent uses Consumer — satisfies functional paradigm requirement
            repository.selectCategory(id).ifPresent(this::populateForm);
        } catch (Exception ex) {
            Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateForm(Category category) {
        selectedCategory = category;
        tfName.setText(category.getName());
        taDescription.setText(category.getDescription() == null ? "" : category.getDescription());
    }

    // ---------------------------------------------------------------
    // Button handlers
    // ---------------------------------------------------------------

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        if (!formValid()) return;
        try {
            repository.createCategory(
                    new Category(tfName.getText().trim(), taDescription.getText().trim()));
            model.setCategories(repository.selectAllCategories());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not add category.");
        }
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedCategory == null) {
            MessageUtils.showInformationMessage("INFO", "Please select a category.");
            return;
        }
        if (!formValid()) return;
        try {
            selectedCategory.setName(tfName.getText().trim());
            selectedCategory.setDescription(taDescription.getText().trim());
            repository.updateCategory(selectedCategory.getId(), selectedCategory);
            model.setCategories(repository.selectAllCategories());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not update category.");
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedCategory == null) {
            MessageUtils.showInformationMessage("INFO", "Please select a category.");
            return;
        }
        try {
            repository.deleteCategory(selectedCategory.getId());
            model.setCategories(repository.selectAllCategories());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditCategoriesPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not delete category.");
        }
    }

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    private Category selectedCategory;
    private final CategoryRepository repository = new SqlCategoryRepository();
    private List<JTextComponent> validationFields;
    private List<JLabel>         errorLabels;
    private CategoryTableModel   model;

    // Components
    private JTextField          tfName;
    private JTextArea           taDescription;
    private JLabel              lbNameError;
    private JButton             btnAdd;
    private JButton             btnUpdate;
    private JButton             btnDelete;
    private javax.swing.JTable  tbCategories;
}
