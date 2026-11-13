package hr.algebra.view;

import hr.algebra.dal.AuthorRepository;
import hr.algebra.dal.CategoryRepository;
import hr.algebra.dal.Repository;
import hr.algebra.dal.RepositoryFactory;
import hr.algebra.dal.sql.SqlAuthorRepository;
import hr.algebra.dal.sql.SqlCategoryRepository;
import hr.algebra.model.Article;
import hr.algebra.model.Author;
import hr.algebra.model.Category;
import hr.algebra.utilities.ArticleService;
import hr.algebra.utilities.FileUtils;
import hr.algebra.utilities.IconUtils;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.view.model.ArticleTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.concurrent.ExecutionException;

public class EditArticlesPanel extends javax.swing.JPanel {

    private final Consumer<Article>          logArticle = a -> System.out.println("Loaded: " + a.getTitle());
    private final Predicate<Article>         hasImage   = a -> a.getPicturePath() != null && !a.getPicturePath().isEmpty();
    private final Function<Article, String>  toTitle    = Article::getTitle;

    private static final int      ALL_CATEGORIES_ID = -1;
    private static final Category ALL_CATEGORIES    = new Category(ALL_CATEGORIES_ID, "All Categories", "");

    public EditArticlesPanel() {
        initComponents();
        init();
    }

    private void initComponents() {
        tfTitle = new JTextField(30);
        tfLink  = new JTextField(30);
        taDesc  = new JTextArea(4, 30);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        tfPath  = new JTextField(25);
        tfPath.setName("PATH");
        tfDate  = new JTextField(20);
        tfDate.setName("DATE");

        lbIcon = new JLabel();
        lbIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/no_image.png")));
        lbIcon.setPreferredSize(new java.awt.Dimension(500, 341));
        

        lbTitleError = createErrorLabel();
        lbLinkError  = createErrorLabel();
        lbDescError  = createErrorLabel();
        lbPathError  = createErrorLabel();
        lbDateError  = createErrorLabel();
        lbAuthor = new JLabel("No author assigned");
        lbAuthor.setForeground(new java.awt.Color(80, 80, 80));

        btnAdd    = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnDelete.setBackground(new java.awt.Color(204, 0, 51));
        btnDelete.setForeground(java.awt.Color.WHITE);
        btnChoose = new JButton("Choose");

        tbArticles = new javax.swing.JTable();

        tfSearch  = new JTextField(20);
        btnSearch = new JButton("Search");
        btnClear  = new JButton("Clear");

        cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new java.awt.Dimension(180, 25));
        cbCategory.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) setText(((Category) value).getName());
                return this;
            }
        });

        spMaxResults = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        spMaxResults.setPreferredSize(new java.awt.Dimension(60, 25));

        rbSortAZ  = new JRadioButton("Sort A→Z", true);
        rbSortZA  = new JRadioButton("Sort Z→A");
        sortGroup = new ButtonGroup();
        sortGroup.add(rbSortAZ);
        sortGroup.add(rbSortZA);

        btnAdd.addActionListener(this::btnAddActionPerformed);
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        btnDelete.addActionListener(this::btnDeleteActionPerformed);
        btnChoose.addActionListener(this::btnChooseActionPerformed);
        btnSearch.addActionListener(this::btnSearchActionPerformed);
        btnClear.addActionListener(e -> clearSearch());

        tbArticles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectArticle(); }
        });
        tbArticles.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { selectArticle(); }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { formComponentShown(e); }
        });

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildSplitPane(),  BorderLayout.CENTER);
        add(buildStatsPanel(), BorderLayout.SOUTH);
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("X");
        lbl.setForeground(new java.awt.Color(204, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new javax.swing.BoxLayout(bar, javax.swing.BoxLayout.Y_AXIS));
        bar.setBorder(BorderFactory.createTitledBorder("Filter"));

        JPanel row1 = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
        row1.add(new JLabel("Keyword:"));
        row1.add(tfSearch);
        row1.add(btnSearch);
        row1.add(btnClear);

        JPanel row2 = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
        row2.add(new JLabel("Category:"));
        row2.add(cbCategory);
        row2.add(new JLabel("  Max results:"));
        row2.add(spMaxResults);
        row2.add(rbSortAZ);
        row2.add(rbSortZA);

        JLabel lblDropZone = new JLabel("Drop article row here to assign Author", JLabel.CENTER);
        lblDropZone.setBorder(BorderFactory.createDashedBorder(java.awt.Color.GRAY, 1f, 6f, 3f, false));
        lblDropZone.setForeground(java.awt.Color.GRAY);
        lblDropZone.setPreferredSize(new java.awt.Dimension(0, 32));
        lblDropZone.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    String idStr = (String) support.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                    showAssignAuthorDialog(Integer.parseInt(idStr.trim()));
                    return true;
                } catch (Exception ex) {
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        });

        bar.add(row1);
        bar.add(row2);
        bar.add(lblDropZone);
        return bar;
    }

    private JSplitPane buildSplitPane() {
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildFormPanel(),
                new JScrollPane(tbArticles));
        split.setResizeWeight(0.55);
        split.setDividerLocation(420);
        return split;
    }

    private JPanel buildFormPanel() {
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);

        addFormRow(fields, gbc, 0, "Title:",          tfTitle, lbTitleError);
        addFormRow(fields, gbc, 1, "Link:",           tfLink,  lbLinkError);
        addTextAreaRow(fields, gbc, 2, "Description:", taDesc, lbDescError);
        addFormRow(fields, gbc, 3, "Published date:", tfDate,  lbDateError);
        cbArticleCategory = new JComboBox<>();
cbArticleCategory.setPreferredSize(new java.awt.Dimension(200, 25));
gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1;
gbc.anchor = GridBagConstraints.EAST;
gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
fields.add(new JLabel("Category:"), gbc);
gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
fields.add(cbArticleCategory, gbc);
gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
fields.add(new JLabel(), gbc);

        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        btnRow.add(btnAdd);
        btnRow.add(btnUpdate);
        gbc.gridy = 3; gbc.gridx = 2; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 6, 5, 6);
        fields.add(lbAuthor, gbc);
       gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 3;
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.anchor = GridBagConstraints.CENTER;
gbc.insets = new Insets(10, 6, 4, 6);
fields.add(btnRow, gbc);

JPanel pathRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
pathRow.add(btnDelete);
pathRow.add(tfPath);
pathRow.add(lbPathError);
pathRow.add(btnChoose);
gbc.gridy = 6; gbc.insets = new Insets(2, 6, 6, 6);
fields.add(pathRow, gbc);
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.add(lbIcon, BorderLayout.CENTER);
        iconPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        iconPanel.setPreferredSize(new java.awt.Dimension(500, 360));

        JPanel form = new JPanel(new BorderLayout(10, 0));
        form.add(fields,     BorderLayout.CENTER);
        form.add(iconPanel,  BorderLayout.EAST);
        return form;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, JTextField field, JLabel error) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(error, gbc);
    }

    private void addTextAreaRow(JPanel panel, GridBagConstraints gbc, int row,
                                String labelText, JTextArea area, JLabel error) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        panel.add(new JScrollPane(area), gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(error, gbc);
    }

 
    private void init() {
        initValidation();
        hideErrors();
        initRepository();
        tbArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbArticles.setAutoCreateRowSorter(true);
        tbArticles.setRowHeight(25);
        tbArticles.setDragEnabled(true);
        tbArticles.setTransferHandler(new ArticleTransferHandler());
        cbCategory.addActionListener(e -> applyFilters());
        spMaxResults.addChangeListener(e -> applyFilters());
        rbSortAZ.addActionListener(e -> applyFilters());
        rbSortZA.addActionListener(e -> applyFilters());
        loadDataAsync();
    }

    private void initValidation() {
        validationFields = Arrays.asList(tfTitle, tfLink, taDesc, tfPath, tfDate);
        errorLabels      = Arrays.asList(lbTitleError, lbLinkError, lbDescError, lbPathError, lbDateError);
    }

    private void initRepository() {
        repository = RepositoryFactory.getRepository();
    }

    private void loadDataAsync() {
        new SwingWorker<List<Article>, Void>() {
            private List<Category> loadedCategories;

            @Override
            protected List<Article> doInBackground() throws Exception {
                loadedCategories = categoryRepository.selectAllCategories();
                return repository.selectArticles();
            }

            @Override
            protected void done() {
                try {
                    allArticles = get();

                    allArticles.forEach(logArticle);

                    List<String> titles = ArticleService.extractField(allArticles, toTitle);
                    boolean someHaveImages = ArticleService.anyMatch(allArticles, hasImage);
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(
                            Level.INFO, "Loaded {0} articles, any with images: {1}",
                            new Object[]{titles.size(), someHaveImages});

                    model = new ArticleTableModel(allArticles);
                    tbArticles.setModel(model);

                    DefaultComboBoxModel<Category> catModel = new DefaultComboBoxModel<>();
                    catModel.addElement(ALL_CATEGORIES);
                    loadedCategories.forEach(catModel::addElement);
                    cbCategory.setModel(catModel);
                    DefaultComboBoxModel<Category> articleCatModel2 = new DefaultComboBoxModel<>();
articleCatModel2.addElement(ALL_CATEGORIES);
loadedCategories.forEach(articleCatModel2::addElement);
cbArticleCategory.setModel(articleCatModel2);
                    applyFilters();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    MessageUtils.showErrorMessage("Load Error", "Article load was interrupted.");
                } catch (ExecutionException ex) {
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    MessageUtils.showErrorMessage("Load Error",
                            "Cannot load articles: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    public void reloadArticles() {
        new SwingWorker<List<Article>, Void>() {
            @Override
            protected List<Article> doInBackground() throws Exception {
                return repository.selectArticles();
            }

            @Override
            protected void done() {
                try {
                    allArticles = get();
                    applyFilters();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }
    
    private void refreshCategoryDropdown() {
        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return categoryRepository.selectAllCategories();
            }

            @Override
            protected void done() {
                try {
                    List<Category> cats = get();
                    Object currentSelection = cbCategory.getSelectedItem();
                    DefaultComboBoxModel<Category> catModel = new DefaultComboBoxModel<>();
                    catModel.addElement(ALL_CATEGORIES);
                    cats.forEach(catModel::addElement);
                    cbCategory.setModel(catModel);
                    DefaultComboBoxModel<Category> articleCatModel = new DefaultComboBoxModel<>();
articleCatModel.addElement(ALL_CATEGORIES);
cats.forEach(articleCatModel::addElement);
cbArticleCategory.setModel(articleCatModel);
cbArticleCategory.setRenderer(new javax.swing.DefaultListCellRenderer() {
    @Override
    public java.awt.Component getListCellRendererComponent(
            javax.swing.JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Category) setText(((Category) value).getName());
        return this;
    }
});
                    cbCategory.setSelectedItem(currentSelection != null ? currentSelection : ALL_CATEGORIES);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }


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
            if ("DATE".equals(validationFields.get(i).getName())) {
                try {
                    LocalDateTime.parse(validationFields.get(i).getText().trim(), Article.DATE_FORMATTER);
                } catch (Exception e) {
                    ok = false;
                    errorLabels.get(i).setVisible(true);
                }
            }
        }
        return ok;
    }

    private void clearForm() {
        hideErrors();
        validationFields.forEach(e -> e.setText(""));
        lbIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/no_image.png")));
        selectedArticle = null;
    }


    private void applyFilters() {
        if (model == null || allArticles == null) return;
        List<Article> result = allArticles;

        Category selectedCategory = (Category) cbCategory.getSelectedItem();
        if (selectedCategory != null && selectedCategory.getId() != ALL_CATEGORIES_ID) {
            final int catId = selectedCategory.getId();
            result = ArticleService.filterAndCollect(result, a -> a.getIdCategory() == catId);
        }

        String keyword = tfSearch.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            Predicate<Article> byKeyword = a -> a.getTitle().toLowerCase().contains(keyword);
            result = ArticleService.filterByKeyword(result, byKeyword);
        }

        Comparator<Article> comparator = Comparator.comparing(Article::getTitle);
        if (rbSortZA.isSelected()) comparator = comparator.reversed();
        result = result.stream().sorted(comparator).collect(Collectors.toList());

        int maxResults = (int) spMaxResults.getValue();
        result = result.stream().limit(maxResults).collect(Collectors.toList());

        refreshTable(result);
    }

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
        applyFilters();
    }

    public List<Article> getArticles() {
        return allArticles;
    }

    public void clearSearch() {
        tfSearch.setText("");
        spMaxResults.setValue(10);
        rbSortAZ.setSelected(true);
        cbCategory.setSelectedIndex(0);   
        if (allArticles != null) refreshTable(allArticles);
    }

    private void refreshTable(List<Article> articles) {
        model.setArticles(articles);
        updateStats();
    }

    private void selectArticle() {
        clearForm();
        int selectedRow = tbArticles.getSelectedRow();
        if (selectedRow < 0) return;
        int id = (int) model.getValueAt(selectedRow, 0);
        try {
            repository.selectArticle(id).ifPresent(a -> {
                selectedArticle = a;
                logArticle.accept(a);  
                philCollins(a);
            });
        } catch (Exception ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void philCollins(Article article) {
        tfTitle.setText(article.getTitle());
        tfLink.setText(article.getLink());
        taDesc.setText(article.getDescription());
        tfDate.setText(article.getPublishedDate().format(Article.DATE_FORMATTER));
        if (article.getPicturePath() != null && Files.exists(Paths.get(article.getPicturePath()))) {
            tfPath.setText(article.getPicturePath());
            setIcon(lbIcon, new File(article.getPicturePath()));
        }
        lbAuthor.setText(article.getIdAuthor() > 0 ? "Author ID: " + article.getIdAuthor() : "No author assigned");
        cbArticleCategory.setSelectedItem(
    ((DefaultComboBoxModel<Category>) cbArticleCategory.getModel())
    .getElementAt(0)); // reset to first
for (int i = 0; i < cbArticleCategory.getItemCount(); i++) {
    if (cbArticleCategory.getItemAt(i).getId() == article.getIdCategory()) {
        cbArticleCategory.setSelectedIndex(i);
        break;
    }
}
    }


    private void btnChooseActionPerformed(java.awt.event.ActionEvent evt) {
        File file = FileUtils.uploadFile("Images", "jpg", "jpeg", "png");
        if (file == null) return;
        tfPath.setText(file.getAbsolutePath());
        setIcon(lbIcon, file);
    }

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        if (!formValid()) return;
        try {
            String localPath = uploadPicture();
            if (localPath == null) return;
            Article article = new Article(
                    tfTitle.getText().trim(),
                    tfLink.getText().trim(),
                    taDesc.getText().trim(),
                    localPath,
                    LocalDateTime.parse(tfDate.getText().trim(), Article.DATE_FORMATTER));
            repository.createArticle(article);
            allArticles = repository.selectArticles();
            clearForm();
            applyFilters();
        } catch (Exception ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedArticle == null) {
            MessageUtils.showInformationMessage("INFO", "Please select");
            return;
        }
        if (!formValid()) return;
        try {
            selectedArticle.setTitle(tfTitle.getText().trim());
            selectedArticle.setLink(tfLink.getText().trim());
            selectedArticle.setDescription(taDesc.getText().trim());
            selectedArticle.setPublishedDate(
                    LocalDateTime.parse(tfDate.getText().trim(), Article.DATE_FORMATTER));
            if (!tfPath.getText().trim().equals(selectedArticle.getPicturePath())
                    && selectedArticle.getPicturePath() != null) {
                Files.deleteIfExists(Paths.get(selectedArticle.getPicturePath()));
                String newPath = uploadPicture();
                if (newPath == null) return;
                selectedArticle.setPicturePath(newPath);
            }
            selectedArticle.setIdCategory(((Category) cbArticleCategory.getSelectedItem()).getId());
            repository.updateArticle(selectedArticle.getId(), selectedArticle);
            allArticles = repository.selectArticles();
            clearForm();
            applyFilters();
        } catch (Exception ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedArticle == null) {
            MessageUtils.showInformationMessage("INFO", "Please select");
            return;
        }
        try {
            if (selectedArticle.getPicturePath() != null) {
                Files.deleteIfExists(Paths.get(selectedArticle.getPicturePath()));
            }
            repository.deleteArticle(selectedArticle.getId());
            allArticles = repository.selectArticles();
            clearForm();
            applyFilters();
        } catch (Exception ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean dirty = false;

    private void formComponentShown(java.awt.event.ComponentEvent evt) {
        if (dirty) {
            dirty = false;
            reloadArticles();
        }
        refreshCategoryDropdown();
    }


    private void setIcon(JLabel label, File file) {
        try {
            label.setIcon(IconUtils.createIcon(file, label.getWidth(), label.getHeight()));
        } catch (IOException ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Error", "Unable to set icon!");
        }
    }

    private static final String DIR = "assets";

    private String uploadPicture() throws IOException {
        String path = tfPath.getText();
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex < 0) {
            MessageUtils.showErrorMessage("Image", "Selected file has no extension.");
            return null;
        }
        String ext  = path.substring(dotIndex);
        String name = UUID.randomUUID() + ext;
        String localPath = DIR + "/" + name;
        FileUtils.copy(path, localPath);
        return localPath;
    }


    private Article selectedArticle;
    private List<Article>     allArticles;
    private Repository        repository;
    private final AuthorRepository   authorRepository   = new SqlAuthorRepository();
    private final CategoryRepository categoryRepository = new SqlCategoryRepository();
    private List<JTextComponent> validationFields;
    private List<JLabel>         errorLabels;
    private ArticleTableModel    model;

    private JTextField          tfTitle;
    private JTextField          tfLink;
    private JTextArea           taDesc;
    private JTextField          tfPath;
    private JTextField          tfDate;
    private JLabel              lbIcon;
    private JLabel              lbTitleError;
    private JLabel              lbLinkError;
    private JLabel              lbDescError;
    private JLabel              lbPathError;
    private JLabel              lbDateError;
    private JLabel              lbAuthor;
    private JButton             btnAdd;
    private JButton             btnUpdate;
    private JButton             btnDelete;
    private JButton             btnChoose;
    private javax.swing.JTable  tbArticles;

    private JTextField                   tfSearch;
    private JButton                      btnSearch;
    private JButton                      btnClear;
    private JComboBox<Category>          cbCategory;
    private JComboBox<Category> cbArticleCategory;
    private JSpinner                     spMaxResults;
    private JRadioButton                 rbSortAZ;
    private JRadioButton                 rbSortZA;
    private ButtonGroup                  sortGroup;
    private JLabel                       lblStats;

    // ---------------------------------------------------------------
    // Drag and drop
    // ---------------------------------------------------------------

    private class ArticleTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (model == null) return null;
            int row = tbArticles.getSelectedRow();
            if (row < 0) return null;
            int articleId = (int) model.getValueAt(row, 0);
            return new StringSelection(String.valueOf(articleId));
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
        }
    }

    private void showAssignAuthorDialog(int articleId) {
        try {
            List<Author> authors = authorRepository.selectAllAuthors();
            if (authors.isEmpty()) {
                MessageUtils.showInformationMessage("Assign Author",
                        "No authors in database. Add authors first.");
                return;
            }

            java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
            javax.swing.JDialog dialog = (parent instanceof javax.swing.JFrame)
                    ? new javax.swing.JDialog((javax.swing.JFrame) parent, "Assign Author", true)
                    : new javax.swing.JDialog((java.awt.Frame) null,       "Assign Author", true);

            JComboBox<Author> cbAuthor = new JComboBox<>(
                    new DefaultComboBoxModel<>(authors.toArray(new Author[0])));
            cbAuthor.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(
                        javax.swing.JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Author) setText(((Author) value).getName());
                    return this;
                }
            });

            JButton btnOk     = new JButton("Assign");
            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(ev -> dialog.dispose());
            btnOk.addActionListener(ev -> {
                Author selected = (Author) cbAuthor.getSelectedItem();
                if (selected != null) {
                    try {
                        repository.assignAuthor(articleId, selected.getId());
                        MessageUtils.showInformationMessage("Assign Author",
                                "Author '" + selected.getName() + "' assigned to article #" + articleId);
                        reloadArticles();
                    } catch (Exception ex) {
                        Logger.getLogger(EditArticlesPanel.class.getName())
                              .log(Level.SEVERE, null, ex);
                        MessageUtils.showErrorMessage("Assign Author", "Assignment failed.");
                    }
                }
                dialog.dispose();
            });

            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            content.add(new JLabel("Assign author to article #" + articleId + ":"),
                    BorderLayout.NORTH);
            content.add(cbAuthor, BorderLayout.CENTER);
            JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            btnRow.add(btnOk);
            btnRow.add(btnCancel);
            content.add(btnRow, BorderLayout.SOUTH);

            dialog.setContentPane(content);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);

        } catch (Exception ex) {
            Logger.getLogger(EditArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Assign Author", "Failed to load authors.");
        }
    }


    private JPanel buildStatsPanel() {
        lblStats = new JLabel("Loading…");
        lblStats.setFont(lblStats.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
        lblStats.setForeground(new java.awt.Color(80, 80, 80));

        JButton btnSurprise = new JButton("Surprise me");
        btnSurprise.setToolTipText("Select a random article from the current list");
        btnSurprise.addActionListener(e -> surpriseMe());

        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 4));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.LIGHT_GRAY));
        panel.add(lblStats);
        panel.add(btnSurprise);
        return panel;
    }

    private void updateStats() {
        if (allArticles == null || allArticles.isEmpty()) {
            lblStats.setText("No articles loaded.");
            return;
        }

        List<Article> unique = ArticleService.distinctById(allArticles);
        Map<String, List<Article>> byAuthor = ArticleService.groupByAuthor(unique);

        String shortest = ArticleService.findShortest(unique)
                .map(Article::getTitle).orElse("—");
        if (shortest.length() > 30) shortest = shortest.substring(0, 30) + "…";

        String longest = ArticleService.findLongest(unique)
                .map(Article::getTitle).orElse("—");
        if (longest.length() > 30) longest = longest.substring(0, 30) + "…";

        boolean someHaveImages = ArticleService.anyMatch(unique, hasImage);
        boolean allHaveImages  = ArticleService.noneMatch(unique, hasImage.negate());

        String imageMsg = allHaveImages  ? "All articles have images"
                        : someHaveImages ? "Some articles missing images"
                                         : "No articles have images";

        lblStats.setText(String.format(
                "%d articles | %d authors | Shortest: \"%s\" | Longest: \"%s\" | %s",
                unique.size(), byAuthor.size(), shortest, longest, imageMsg));
    }

    private void surpriseMe() {
        if (allArticles == null || allArticles.isEmpty() || model == null) return;

        int skip = new Random().nextInt(allArticles.size());
        Optional<Article> pick = ArticleService.findFirstSorted(allArticles, skip);
        if (!pick.isPresent()) {
            pick = ArticleService.findAny(allArticles, hasImage);
        }

        pick.ifPresent(article -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((int) model.getValueAt(i, 0) == article.getId()) {
                    tbArticles.setRowSelectionInterval(i, i);
                    tbArticles.scrollRectToVisible(tbArticles.getCellRect(i, 0, true));
                    selectArticle();
                    return;
                }
            }
            MessageUtils.showInformationMessage("Surprise me",
                    "\"" + article.getTitle() + "\" is hidden by the current filter. Clear the filter to see it.");
        });
    }
}
