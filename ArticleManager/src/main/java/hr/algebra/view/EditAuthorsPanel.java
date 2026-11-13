package hr.algebra.view;

import hr.algebra.dal.AuthorRepository;
import hr.algebra.dal.sql.SqlAuthorRepository;
import hr.algebra.model.Author;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.view.model.AuthorTableModel;
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

public class EditAuthorsPanel extends javax.swing.JPanel {

    public EditAuthorsPanel() {
        initComponents();
        init();
    }

    private void initComponents() {
        tfName  = new JTextField(30);
        taBio   = new JTextArea(3, 30);
        taBio.setLineWrap(true);
        taBio.setWrapStyleWord(true);
        tfEmail = new JTextField(30);
        lbNameError  = createErrorLabel();
        lbEmailError = createErrorLabel();
        btnAdd    = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnDelete.setBackground(new Color(204, 0, 51));
        btnDelete.setForeground(Color.WHITE);
        tbAuthors = new javax.swing.JTable();

        btnAdd.addActionListener(this::btnAddActionPerformed);
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        tbAuthors.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectEntity(); }
        });
        tbAuthors.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { selectEntity(); }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildFormPanel(),
                new JScrollPane(tbAuthors));
        splitPane.setResizeWeight(0.40);
        splitPane.setDividerLocation(230);

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

        // row 1 — Bio (optional)
        gbc.gridy = 1; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Bio:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        form.add(new JScrollPane(taBio), gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel(""), gbc); // spacer keeps grid aligned

        // row 2 — Email
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.weighty = 0;
        form.add(tfEmail, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(lbEmailError, gbc);

        // row 3 — buttons
        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 3;
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
        // Only Name is required; Bio and Email are optional
        validationFields = Arrays.asList(tfName);
        errorLabels      = Arrays.asList(lbNameError);
    }

    private void initTable() {
        tbAuthors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbAuthors.setAutoCreateRowSorter(true);
        tbAuthors.setRowHeight(25);
        model = new AuthorTableModel(new ArrayList<>());
        tbAuthors.setModel(model);

        new SwingWorker<List<Author>, Void>() {
            @Override
            protected List<Author> doInBackground() throws Exception {
                return repository.selectAllAuthors();
            }

            @Override
            protected void done() {
                try {
                    model.setAuthors(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
                    MessageUtils.showErrorMessage("Load Error", "Cannot load authors: " +
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
        taBio.setText("");
        tfEmail.setText("");
        selectedAuthor = null;
    }

    // ---------------------------------------------------------------
    // Table selection
    // ---------------------------------------------------------------

    private void selectEntity() {
        clearForm();
        int row = tbAuthors.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        try {
            // ifPresent uses Consumer — satisfies functional paradigm requirement
            repository.selectAuthor(id).ifPresent(this::populateForm);
        } catch (Exception ex) {
            Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateForm(Author author) {
        selectedAuthor = author;
        tfName.setText(author.getName());
        taBio.setText(author.getBio() == null ? "" : author.getBio());
        tfEmail.setText(author.getEmail() == null ? "" : author.getEmail());
    }

    // ---------------------------------------------------------------
    // Button handlers
    // ---------------------------------------------------------------

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        if (!formValid()) return;
        try {
            repository.createAuthor(new Author(
                    tfName.getText().trim(),
                    taBio.getText().trim(),
                    tfEmail.getText().trim()));
            model.setAuthors(repository.selectAllAuthors());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not add author.");
        }
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedAuthor == null) {
            MessageUtils.showInformationMessage("INFO", "Please select an author.");
            return;
        }
        if (!formValid()) return;
        try {
            selectedAuthor.setName(tfName.getText().trim());
            selectedAuthor.setBio(taBio.getText().trim());
            selectedAuthor.setEmail(tfEmail.getText().trim());
            repository.updateAuthor(selectedAuthor.getId(), selectedAuthor);
            model.setAuthors(repository.selectAllAuthors());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not update author.");
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedAuthor == null) {
            MessageUtils.showInformationMessage("INFO", "Please select an author.");
            return;
        }
        try {
            repository.deleteAuthor(selectedAuthor.getId());
            model.setAuthors(repository.selectAllAuthors());
            clearForm();
        } catch (Exception ex) {
            Logger.getLogger(EditAuthorsPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Could not delete author.");
        }
    }

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    private Author selectedAuthor;
    private final AuthorRepository repository = new SqlAuthorRepository();
    private List<JTextComponent> validationFields;
    private List<JLabel>         errorLabels;
    private AuthorTableModel     model;

    // Components
    private JTextField          tfName;
    private JTextArea           taBio;
    private JTextField          tfEmail;
    private JLabel              lbNameError;
    private JLabel              lbEmailError;
    private JButton             btnAdd;
    private JButton             btnUpdate;
    private JButton             btnDelete;
    private javax.swing.JTable  tbAuthors;
}
