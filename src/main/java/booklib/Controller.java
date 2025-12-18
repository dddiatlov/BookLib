package booklib;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import booklib.books.Book;
import booklib.books.BookDao;
import booklib.readingSessions.ReadingSession;
import booklib.readingSessions.ReadingSessionController;
import booklib.readingSessions.ReadingSessionDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import booklib.books.AddBookController;
import javafx.geometry.Pos;

import java.util.List;

public class Controller {

    private static final String STATUS_WANT = "WANT_TO_READ";
    private static final String STATUS_READING = "READING";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String THEME_LIGHT = "/styles/light-theme.css";
    private static final String THEME_DARK  = "/styles/dark-theme.css";

    @FXML private Button logoutButton;
    @FXML private VBox booksContainer;
    @FXML private ListView<ReadingSession> readingSessionsListView;
    @FXML private ToggleButton themeToggle;

    private final BookDao bookDao = Factory.INSTANCE.getBookDao();
    private final ReadingSessionDao readingSessionDao = Factory.INSTANCE.getReadingSessionDao();
    private final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(getClass());

    @FXML
    public void initialize() {
        setupReadingSessionsListCell();
        refreshMyBooksCards();
        boolean dark = prefs.getBoolean("theme.dark", false);
        themeToggle.setSelected(dark);
        applyTheme(dark);
        themeToggle.setText(dark ? "Light" : "Dark");
    }

    @FXML
    private void onThemeToggle() {
        boolean dark = themeToggle.isSelected();
        prefs.putBoolean("theme.dark", dark);

        applyTheme(dark);
        themeToggle.setText(dark ? "Light" : "Dark");
    }

    private void applyTheme(boolean dark) {
        var scene = themeToggle.getScene();
        if (scene == null) return;

        scene.getStylesheets().remove(getClass().getResource(THEME_LIGHT).toExternalForm());
        scene.getStylesheets().remove(getClass().getResource(THEME_DARK).toExternalForm());

        String css = dark ? THEME_DARK : THEME_LIGHT;
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    private void refreshMyBooksCards() {
        try {
            long readerId = Session.requireReaderId();

            // 1) favorites
            List<Book> favorites = bookDao.findFavoritesByReaderId(readerId);

            // 2) all my books
            List<Book> books = bookDao.findByReaderId(readerId);

            var favIds = favorites.stream().map(Book::getId).collect(java.util.stream.Collectors.toSet());
            books.removeIf(b -> favIds.contains(b.getId()));

            booksContainer.getChildren().clear();

            // ===== Favorites block =====
            if (!favorites.isEmpty()) {
                Label favHeader = new Label("Favorites");
                favHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0 0 6 0;");
                booksContainer.getChildren().add(favHeader);

                for (Book b : favorites) {
                    booksContainer.getChildren().add(createBookCard(readerId, b));
                }

                Separator sep = new Separator();
                sep.setStyle("-fx-padding: 10 0 10 0;");
                booksContainer.getChildren().add(sep);
            }

            // ===== My Books block =====
            Label myHeader = new Label("My Books");
            myHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0 0 6 0;");
            booksContainer.getChildren().add(myHeader);

            for (Book book : books) {
                booksContainer.getChildren().add(createBookCard(readerId, book));
            }

            readingSessionsListView.getItems().clear();

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("DB error", e.getMessage());
        }
    }

    private HBox createBookCard(long readerId, Book book) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("book-card");

        // Щоб картка займала всю доступну ширину (і ресайзилась разом зі списком)
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        if (booksContainer != null) {
            // мінус padding контейнера (підкоригуйте, якщо у вас інші відступи)
            card.prefWidthProperty().bind(booksContainer.widthProperty().subtract(20));
        }

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setWrapText(false);
        title.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label statusLabel = new Label(book.getStatus() == null ? STATUS_WANT : book.getStatus());
        statusLabel.setStyle(statusStyle(statusLabel.getText()));
        statusLabel.setMinWidth(120);
        statusLabel.setPrefWidth(120);
        statusLabel.setMaxWidth(120);
        statusLabel.setAlignment(Pos.CENTER_LEFT);

        boolean fav = bookDao.isFavorite(readerId, book.getId());

        Button favBtn = new Button(fav ? "★" : "☆");
        setFixedButton(favBtn, 35);

        Button sessionsBtn = new Button("Sessions");
        setFixedButton(sessionsBtn, 80);

        Button addSessionBtn = new Button("+");
        setFixedButton(addSessionBtn, 32);

        Button deleteBtn = new Button("Delete");
        setFixedButton(deleteBtn, 70);

        sessionsBtn.setOnAction(e -> loadReadingSessionsForBook(book));

        if (isBookFinished(readerId, book)) {
            addSessionBtn.setDisable(true);
        } else {
            addSessionBtn.setOnAction(e -> openReadingSessionDialog(book, null));
        }

        deleteBtn.setOnAction(e -> deleteBook(book));

        favBtn.setOnAction(e -> {
            try {
                boolean makeFavNow = "☆".equals(favBtn.getText());
                if (makeFavNow) bookDao.addFavorite(readerId, book.getId());
                else bookDao.removeFavorite(readerId, book.getId());
                refreshMyBooksCards();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alerts.error("DB error", ex.getMessage());
            }
        });

        HBox left = new HBox(10, title);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setMinWidth(0);
        HBox.setHgrow(left, Priority.ALWAYS);

        HBox right = new HBox(8, statusLabel, favBtn, sessionsBtn, addSessionBtn, deleteBtn);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setMinWidth(Region.USE_PREF_SIZE);
        right.setMaxWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().setAll(left, spacer, right);
        return card;
    }

    private void setFixedButton(Button btn, double width) {
        btn.setMinWidth(width);
        btn.setPrefWidth(width);
        btn.setMaxWidth(width);
    }

    private String statusStyle(String status) {
        return switch (status) {
            case STATUS_FINISHED -> "-fx-text-fill: #1b7f2a; -fx-font-weight: bold;";
            case STATUS_READING -> "-fx-text-fill: #c47f00; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #666666; -fx-font-weight: bold;";
        };
    }

    private void deleteBook(Book book) {
        long readerId = Long.valueOf(Session.requireReaderId());

        var confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this book from your list?\n\n" + book.getTitle(),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        var res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try {
                bookDao.removeBookForReader(book.getId(), readerId);
                refreshMyBooksCards();
            } catch (Exception e) {
                e.printStackTrace();
                Alerts.error("DB error", e.getMessage());
            }
        }
    }

    /* =========================
       READING SESSIONS
       ========================= */

    private void loadReadingSessionsForBook(Book book) {
        try {
            long readerId = Session.requireReaderId();
            var sessions = readingSessionDao.findByReaderIdAndBookId(readerId, book.getId());
            readingSessionsListView.getItems().setAll(sessions);

            // also keep status synced whenever sessions are viewed
            syncBookStatus(readerId, book);

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("DB error", e.getMessage());
        }
    }

    private void setupReadingSessionsListCell() {
        readingSessionsListView.setCellFactory(list -> new ListCell<>() {

            private final Label lbl = new Label();
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, lbl, editBtn, delBtn);

            {
                lbl.setWrapText(true);
                lbl.setMinWidth(0);
                lbl.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lbl, Priority.ALWAYS);

                setFixedButton(editBtn, 40);
                setFixedButton(delBtn, 55);

                box.setAlignment(Pos.CENTER_LEFT);
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(ReadingSession item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                lbl.setText(item.toString());

                editBtn.setOnAction(e -> openReadingSessionDialog(item.getBook(), item));

                delBtn.setOnAction(e -> {
                    var confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete this session?\n\n" + item,
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);

                    var res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        try {
                            readingSessionDao.delete(item.getId());

                            Book b = item.getBook();
                            if (b != null) {
                                long readerId = Session.requireReaderId();
                                syncBookStatus(readerId, b);
                                refreshMyBooksCards();
                                loadReadingSessionsForBook(b);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alerts.error("DB error", ex.getMessage());
                        }
                    }
                });

                setGraphic(box);
            }

            private void setFixedButton(Button btn, double width) {
                btn.setMinWidth(width);
                btn.setPrefWidth(width);
                btn.setMaxWidth(width);
            }
        });
    }

    private void openReadingSessionDialog(Book book, ReadingSession toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/booklib/ReadingSessionView.fxml"));
            Parent root = loader.load();

            ReadingSessionController c = loader.getController();
            c.setBook(book);
            if (toEdit != null) c.setEditingSession(toEdit);

            Stage st = new Stage();
            st.setTitle(toEdit == null ? "Add Reading Session" : "Edit Reading Session");
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.showAndWait();

            // after dialog: recalc status + refresh UI + reload sessions
            long readerId = Session.requireReaderId();
            syncBookStatus(readerId, book);
            refreshMyBooksCards();
            loadReadingSessionsForBook(book);

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("UI error", e.getMessage());
        }
    }

    /**
     * Computes and persists correct status for (reader, book) based on reading sessions.
     */
    private void syncBookStatus(long readerId, Book book) {
        if (book == null || book.getId() == null) return;

        int totalRead = readingSessionDao.sumPagesForReaderAndBook(readerId, book.getId());
        int totalPages = Math.max(0, book.getPages());

        String status;
        if (totalRead <= 0) status = STATUS_WANT;
        else if (totalRead >= totalPages && totalPages > 0) status = STATUS_FINISHED;
        else status = STATUS_READING;

        // persist into book_status (your method is UPSERT)
        bookDao.addBookForReader(book.getId(), readerId, status);

        // keep object consistent for UI
        book.setStatus(status);
    }

    private boolean isBookFinished(long readerId, Book book) {
        if (book == null || book.getId() == null) return false;

        int totalPages = Math.max(0, book.getPages());
        if (totalPages == 0) return false; // safety

        int totalRead = readingSessionDao.sumPagesForReaderAndBook(readerId, book.getId());
        return totalRead >= totalPages;
    }

    /* =========================
       BOOKS
       ========================= */

    @FXML
    public void onAddBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/booklib/AddBookView.fxml"));
            Parent root = loader.load();

            AddBookController c = loader.getController();

            Stage st = new Stage();
            st.setTitle("Add Book");
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.showAndWait();

            if (c.isSaved()) {
                refreshMyBooksCards();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("UI error", e.getMessage());
        }
    }

    @FXML
    public void onImportCsv() {
        try (InputStream in = getClass().getResourceAsStream("/booklib/books.csv")) {
            if (in == null) {
                Alerts.error("CSV import", "Resource /booklib/books.csv not found");
                return;
            }

            Path tmp = Files.createTempFile("booklib-books-", ".csv");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);

            int loaded = bookDao.loadFromCsv(tmp.toFile());

            Alerts.info("CSV import", "Loaded: " + loaded + " books.");
        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("CSV import error", e.getMessage());
        }
    }

    @FXML
    public void onLogout(ActionEvent event) {
        Session.clear();
        Node source = (Node) event.getSource();
        SceneSwitcher.switchTo("/booklib/LoginView.fxml", source);
    }
}
