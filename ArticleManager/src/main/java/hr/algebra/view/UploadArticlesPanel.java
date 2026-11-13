package hr.algebra.view;

import hr.algebra.dal.Repository;
import hr.algebra.dal.RepositoryFactory;
import hr.algebra.model.Article;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.utilities.RssParseException;
import hr.algebra.utilities.RssParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

public class UploadArticlesPanel extends javax.swing.JPanel {

    private static final String RSS_URL =
        "https://www.nasa.gov/feed/";

    private Repository repository;
    private DefaultListModel<Article> listModel;

    public UploadArticlesPanel() {
        initComponents();
        init();
    }

    private void initComponents() {
        lsArticles = new javax.swing.JList<>();
        btnUpload  = new javax.swing.JButton("Upload");
        lbStatus   = new javax.swing.JLabel("");

        lbStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbStatus.setForeground(new java.awt.Color(0, 100, 0));

        btnUpload.addActionListener(this::btnUploadActionPerformed);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(lsArticles);

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel(
                new java.awt.BorderLayout(0, 6));
        bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottomPanel.add(lbStatus,   java.awt.BorderLayout.NORTH);
        bottomPanel.add(btnUpload,  java.awt.BorderLayout.CENTER);

        setLayout(new java.awt.BorderLayout(0, 0));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(scrollPane,   java.awt.BorderLayout.CENTER);
        add(bottomPanel,  java.awt.BorderLayout.SOUTH);
    }

    private void init() {
        repository = RepositoryFactory.getRepository();
        listModel  = new DefaultListModel<>();
        lsArticles.setModel(listModel);

        new SwingWorker<List<Article>, Void>() {
            @Override
            protected List<Article> doInBackground() throws Exception {
                return repository.selectArticles();
            }

            @Override
            protected void done() {
                try {
                    get().forEach(listModel::addElement);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(UploadArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(UploadArticlesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    MessageUtils.showErrorMessage("Error", "Could not load articles from database.");
                }
            }
        }.execute();
    }

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {
        btnUpload.setEnabled(false);
        lbStatus.setText("Importing articles — please wait…");

        SwingWorker<List<Article>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Article> doInBackground() throws Exception {
                List<Article> parsed = RssParser.parse(RSS_URL);

                Set<Article> uniqueArticles = new TreeSet<>(
                        Comparator.comparing(Article::getLink));
                uniqueArticles.addAll(parsed);

                repository.createArticles(new ArrayList<>(uniqueArticles));
                return repository.selectArticles();
            }

            @Override
            protected void done() {
                btnUpload.setEnabled(true);
                try {
                    List<Article> articles = get();
                    listModel.clear();
                    articles.forEach(listModel::addElement);

                    EditArticlesPanel.dirty = true;

                    lbStatus.setText("Done");
                    MessageUtils.showInformationMessage(
                            "RSS Import",
                            "Import complete. " + articles.size() + " articles in database.");

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    lbStatus.setText("Import failed");
                    MessageUtils.showErrorMessage("Import cancelled",
                            "The import was interrupted.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    Logger.getLogger(UploadArticlesPanel.class.getName())
                          .log(Level.SEVERE, null, ex);
                    String detail = (cause instanceof RssParseException)
                            ? cause.getMessage()
                            : "An unexpected error occurred during import.";
                    lbStatus.setText("Import failed");
                    MessageUtils.showErrorMessage("Import failed", detail);
                }
            }
        };

        worker.execute();
    }
    
    public void reloadAfterWipe() {
    listModel.clear();
}

    private javax.swing.JList<Article>  lsArticles;
    private javax.swing.JButton         btnUpload;
    private javax.swing.JLabel          lbStatus;
}
