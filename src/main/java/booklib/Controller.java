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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import booklib.books.AddBookController;

import java.util.List;

public class Controller {
    @FXML
    private Button logoutButton;

    private static final String STATUS_WANT = "WANT_TO_READ";

    @FXML private VBox booksContainer;
    @FXML private ListView<ReadingSession> readingSessionsListView;

    private final BookDao bookDao = Factory.INSTANCE.getBookDao();
    private final ReadingSessionDao readingSessionDao = Factory.INSTANCE.getReadingSessionDao();

    @FXML
    private ListView<Book> allBooksList;



    @FXML
    public void initialize() {
        setupReadingSessionsListCell();

        // 🔹 красивое отображение книг в All Books
        allBooksList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setText(null);
                } else {
                    setText(b.getTitle()
                            + (b.getAuthor() != null && !b.getAuthor().isBlank()
                            ? " — " + b.getAuthor()
                            : ""));
                }
            }
        });

        loadAllBooks();
        refreshMyBooksCards();
    }


    private void loadAllBooks() {
        allBooksList.getItems().setAll(bookDao.findAll());
    }
    @FXML
    public void onAddToMyBooks() {
        Book book = allBooksList.getSelectionModel().getSelectedItem();
        if (book == null) {
            Alerts.error("Select a book", "Please select a book first.");
            return;
        }

        Long readerId = Session.getCurrentReader().getId();
        bookDao.addBookForReader(book.getId(), readerId, "WANT_TO_READ");

        refreshMyBooksCards();
    }


    /* =========================
       BOOKS (cards in VBox)
       ========================= */

    private void refreshMyBooksCards() {
        try {
            Long readerId = Long.valueOf(Session.requireReaderId());
            List<Book> books = bookDao.findByReaderId(readerId);

            booksContainer.getChildren().clear();

            for (Book book : books) {
                booksContainer.getChildren().add(createBookCard(book));
            }

            readingSessionsListView.getItems().clear();

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("DB error", e.getMessage());
        }
    }

    private HBox createBookCard(Book book) {
        HBox card = new HBox(10);
        card.setStyle("""
                -fx-padding: 10;
                -fx-background-color: white;
                -fx-border-color: #dcdcdc;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                """);

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button sessionsBtn = new Button("Sessions");
        sessionsBtn.setOnAction(e -> loadReadingSessionsForBook(book));

        Button addSessionBtn = new Button("+");
        addSessionBtn.setOnAction(e -> openReadingSessionDialog(book, null));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteBook(book));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(title, spacer, sessionsBtn, addSessionBtn, deleteBtn);
        return card;
    }

    private void deleteBook(Book book) {
        Long readerId = Long.valueOf(Session.requireReaderId());

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
        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("DB error", e.getMessage());
        }
    }

    private void setupReadingSessionsListCell() {
        readingSessionsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ReadingSession item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label lbl = new Label(item.toString());
                HBox.setHgrow(lbl, Priority.ALWAYS);

                Button editBtn = new Button("Edit");
                editBtn.setOnAction(e -> openReadingSessionDialog(item.getBook(), item));

                Button delBtn = new Button("Delete");
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
                            if (b != null) loadReadingSessionsForBook(b);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alerts.error("DB error", ex.getMessage());
                        }
                    }
                });

                HBox box = new HBox(10, lbl, editBtn, delBtn);
                setGraphic(box);
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

            loadReadingSessionsForBook(book);

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("UI error", e.getMessage());
        }
    }
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
                refreshMyBooksCards(); // обновить карточки сразу после добавления
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
                Alerts.error("CSV import", "Resource /booklib/books.csv not found (check src/main/resources/booklib/books.csv)");
                return;
            }

            // копируем ресурс во временный файл, чтобы использовать твой существующий loadFromCsv(File)
            Path tmp = Files.createTempFile("booklib-books-", ".csv");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);

            int loaded = bookDao.loadFromCsv(tmp.toFile());

            Alerts.info("CSV import", "Loaded: " + loaded + " books.");
            // обнови UI если нужно:
            // refreshAllBooksCards(); или refreshMyBooksCards(); — что у тебя есть

        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("CSV import error", e.getMessage());
        }
    }
    @FXML
    public void onLogout() {
        Session.clear();

        try {
            SceneSwitcher.switchTo(
                    "/booklib/LoginView.fxml",
                    logoutButton
                        );
        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("Logout failed", "Unable to return to login screen.");
        }
    }





}

