package hr.algebra;

import com.formdev.flatlaf.FlatLightLaf;
import hr.algebra.dal.RepositoryFactory;
import hr.algebra.dal.UserRepository;
import hr.algebra.dal.sql.SqlUserRepository;
import hr.algebra.model.Article;
import hr.algebra.model.ArticleList;
import hr.algebra.model.User;
import hr.algebra.utilities.FileUtils;
import hr.algebra.utilities.JaxbUtils;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.view.EditArticlesPanel;
import hr.algebra.view.EditAuthorsPanel;
import hr.algebra.view.EditCategoriesPanel;
import hr.algebra.view.UploadArticlesPanel;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author daniel.bele
 */
public class ArticleManager extends javax.swing.JFrame {

    private User currentUser;

    public ArticleManager() {
        initComponents();
        configurePanels();
        configureMenu();
    }

    public ArticleManager(User user) {
        this.currentUser = user;
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        configurePanels();
        configureMenu();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpContent = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Article manager");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tpContent, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tpContent, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatLightLaf.setup();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tpContent;
    // End of variables declaration//GEN-END:variables

    private EditArticlesPanel   editArticlesPanel;
    private UploadArticlesPanel uploadArticlesPanel;
    private final UserRepository userRepository = new SqlUserRepository();

    private void configurePanels() {
        uploadArticlesPanel = new UploadArticlesPanel();
        editArticlesPanel   = new EditArticlesPanel();
        tpContent.add(UPLOAD_ARTICLES, uploadArticlesPanel);
        tpContent.add(EDIT_ARTICLES,   editArticlesPanel);
        tpContent.add(EDIT_CATEGORIES, new EditCategoriesPanel());
        tpContent.add(EDIT_AUTHORS,    new EditAuthorsPanel());
    }

    private void configureMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu menuFile = new JMenu("File");
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.setMnemonic('X');
        miExit.addActionListener(e -> System.exit(0));
        menuFile.add(miExit);

        // Actions menu
        JMenu menuActions = new JMenu("Actions");
        JMenuItem miImportRss = new JMenuItem("Import RSS");
        miImportRss.addActionListener(e -> tpContent.setSelectedComponent(uploadArticlesPanel));
        JMenuItem miExportXml = new JMenuItem("Export XML");
        miExportXml.addActionListener(e -> exportXml());
        JMenuItem miClearSearch = new JMenuItem("Clear Search");
        miClearSearch.addActionListener(e -> editArticlesPanel.clearSearch());
        menuActions.add(miImportRss);
        menuActions.add(miExportXml);
        menuActions.add(miClearSearch);

        // Admin menu 
        JMenu menuAdmin = new JMenu("Admin");
        JMenuItem miWipeData = new JMenuItem("Wipe All Data");
        miWipeData.setEnabled(currentUser != null && userRepository.isAdmin(currentUser));
        miWipeData.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    ArticleManager.this,
                    "Delete ALL articles from the database. This cannot be undone.",
                    "Wipe All Data",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            miWipeData.setEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RepositoryFactory.getRepository().deleteAllArticles();
                    return null;
                }

                @Override
                protected void done() {
                    miWipeData.setEnabled(currentUser != null && userRepository.isAdmin(currentUser));
                    try {
                        get();
                        editArticlesPanel.reloadArticles();
                        uploadArticlesPanel.reloadAfterWipe();
                        MessageUtils.showInformationMessage("Wipe All Data",
                                "All articles have been deleted.");
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        MessageUtils.showErrorMessage("Wipe All Data", "Operation was interrupted.");
                    } catch (Exception ex) {
                        Logger.getLogger(ArticleManager.class.getName()).log(Level.SEVERE, null, ex);
                        MessageUtils.showErrorMessage("Wipe All Data",
                                "Delete failed: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    }
                }
            }.execute();
        });
        menuAdmin.add(miWipeData);

        menuBar.add(menuFile);
        menuBar.add(menuActions);
        menuBar.add(menuAdmin);
        setJMenuBar(menuBar);
    }

    private void exportXml() {
        File file = FileUtils.chooseXmlSaveFile(this);
        if (file == null) return;

        try {
            List<Article> articles = editArticlesPanel.getArticles();
            JaxbUtils.exportToXml(new ArticleList(articles), file);
            MessageUtils.showInformationMessage("Export XML",
                    "Exported " + articles.size() + " articles to " + file.getName());
        } catch (Exception ex) {
            Logger.getLogger(ArticleManager.class.getName()).log(Level.SEVERE, null, ex);
            MessageUtils.showErrorMessage("Export XML", "Export failed: " + ex.getMessage());
        }
    }

    private static final String EDIT_ARTICLES   = "Edit articles";
    private static final String UPLOAD_ARTICLES = "Upload articles";
    private static final String EDIT_CATEGORIES = "Categories";
    private static final String EDIT_AUTHORS    = "Authors";
}
