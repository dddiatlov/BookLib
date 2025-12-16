package booklib;

import booklib.books.Book;
import booklib.books.BookDao;
import booklib.readingSessions.ReadingSession;
import booklib.readingSessions.ReadingSessionController;
import booklib.readingSessions.ReadingSessionDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Controller {

    private static final long CURRENT_READER_ID = 1L;

    // Must match ENUM in init.sql:
    private static final String STATUS_WANT_TO_READ = "WANT_TO_READ";
    private static final String STATUS_READING = "READING";
    private static final String STATUS_FINISHED = "FINISHED";

    private final BookDao bookDao = Factory.INSTANCE.getBookDao();
    private final ReadingSessionDao readingSessionDao = Factory.INSTANCE.getReadingSessionDao();

    private final ObservableList<Book> myBooks = FXCollections.observableArrayList();
    private Book selectedBook = null;

    @FXML private VBox booksContainer;
    @FXML private ListView<ReadingSession> readingSessionsListView;

    @FXML private Button addBookButton;
    @FXML private Label myBooksLabel;

    @FXML
    private void initialize() {
        myBooksLabel.setText("My Books");

        configureReadingSessionsListView();

        try {
            loadBooksFromResourcesCsv();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Cannot load /booklib/books.csv from resources.\n" +
                            "Expected: src/main/resources/booklib/books.csv\n\n" + e.getMessage()
            ).showAndWait();
        }

        // Load My Books from DB
        try {
            myBooks.setAll(bookDao.findByReaderId(CURRENT_READER_ID));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load My Books from DB:\n" + e.getMessage()).showAndWait();
        }

        refreshMyBooksUI();

        if (!myBooks.isEmpty()) {
            selectedBook = myBooks.get(0);
            refreshReadingSessionsForBook(selectedBook);
        } else {
            clearSessionsList("Select a book to see sessions");
        }
    }

    private void configureReadingSessionsListView() {
        if (readingSessionsListView == null) return;

        readingSessionsListView.setPlaceholder(new Label("Select a book to see sessions"));

        readingSessionsListView.setCellFactory(lv -> {
            ListCell<ReadingSession> cell = new ListCell<>() {
                @Override
                protected void updateItem(ReadingSession item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            };

            MenuItem edit = new MenuItem("Edit");
            edit.setOnAction(e -> {
                ReadingSession s = cell.getItem();
                if (s == null) return;
                openEditReadingSession(s);
                refreshReadingSessionsForSelectedBook();
            });

            MenuItem del = new MenuItem("Delete");
            del.setOnAction(e -> {
                ReadingSession s = cell.getItem();
                if (s == null) return;

                var confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete this session?\n\n" + s.toString(),
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);

                var res = confirm.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.YES) {
                    try {
                        readingSessionDao.delete(s.getId());
                        refreshReadingSessionsForSelectedBook();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Failed to delete session:\n" + ex.getMessage()).showAndWait();
                    }
                }
            });

            ContextMenu menu = new ContextMenu(edit, del);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> cell.setContextMenu(isNowEmpty ? null : menu));

            return cell;
        });
    }

    @FXML
    private void onAddBook() {
        var allBooks = bookDao.findAll();
        if (allBooks.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No available books loaded from CSV.").showAndWait();
            return;
        }

        var availableToAdd = allBooks.stream()
                .filter(b -> !containsById(myBooks, b.getId()))
                .toList();

        if (availableToAdd.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "All available books are already in My Books.").showAndWait();
            return;
        }

        // ---- Dialog content: Book list + Status combo ----
        ListView<Book> listView = new ListView<>();
        listView.getItems().addAll(availableToAdd);

        listView.setCellFactory(lv -> {
            ListCell<Book> cell = new ListCell<>() {
                @Override
                protected void updateItem(Book book, boolean empty) {
                    super.updateItem(book, empty);
                    if (empty || book == null) {
                        setText(null);
                    } else {
                        String pages = (book.getPages() != null) ? (" (" + book.getPages() + " pages)") : "";
                        setText(book.getTitle() + pages);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    showBookInfo(cell.getItem());
                    event.consume();
                }
            });

            return cell;
        });

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(STATUS_WANT_TO_READ, STATUS_READING, STATUS_FINISHED);
        statusBox.setValue(STATUS_WANT_TO_READ);

        HBox statusRow = new HBox(10, new Label("Status:"), statusBox);

        VBox content = new VBox(10, listView, statusRow);
        content.setPrefWidth(420);

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book to My Books");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == addBtn ? listView.getSelectionModel().getSelectedItem() : null);

        var result = dialog.showAndWait();
        result.ifPresent(book -> {
            String status = statusBox.getValue() != null ? statusBox.getValue() : STATUS_WANT_TO_READ;

            // persist to DB with valid ENUM value
            try {
                bookDao.addBookForReader(book.getId(), CURRENT_READER_ID, status);
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to add book to DB:\n" + ex.getMessage()).showAndWait();
                return;
            }

            book.setStatus(status); // IMPORTANT: so UI shows correct status immediately
            myBooks.add(book);
            selectedBook = book;
            refreshMyBooksUI();
            refreshReadingSessionsForBook(book);
        });
    }

    private void refreshMyBooksUI() {
        booksContainer.getChildren().clear();

        if (myBooks.isEmpty()) {
            Label empty = new Label("No books in My Books yet. Click + Add Book.");
            empty.setStyle("-fx-text-fill: #666;");
            booksContainer.getChildren().add(empty);
            return;
        }

        for (Book b : myBooks) {
            booksContainer.getChildren().add(createMyBookRow(b));
        }
    }

    private HBox createMyBookRow(Book book) {
        // Left block: title + status
        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label status = new Label(formatStatus(book.getStatus()));
        status.setStyle("""
            -fx-padding: 2 8 2 8;
            -fx-background-color: #eef2ff;
            -fx-border-color: #c7d2fe;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-text-fill: #1f2937;
            """);

        VBox left = new VBox(6, title, status);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.setMaxWidth(Double.MAX_VALUE);

        // clicking selects book
        left.setOnMouseClicked(e -> {
            selectedBook = book;
            refreshReadingSessionsForBook(book);
        });

        Button addSession = new Button("Add session");
        addSession.setOnAction(e -> {
            selectedBook = book;
            openCreateReadingSession(book);
            refreshReadingSessionsForBook(book);
        });

        Button remove = new Button("Remove");
        remove.setOnAction(e -> {
            try {
                bookDao.removeBookForReader(book.getId(), CURRENT_READER_ID);
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to remove book from DB:\n" + ex.getMessage()).showAndWait();
                return;
            }

            myBooks.removeIf(b -> safeEqId(b.getId(), book.getId()));
            refreshMyBooksUI();

            if (selectedBook != null && safeEqId(selectedBook.getId(), book.getId())) {
                selectedBook = null;
                clearSessionsList("Select a book to see sessions");
            }
        });

        HBox row = new HBox(12, left, addSession, remove);
        row.setStyle("""
            -fx-padding: 10;
            -fx-background-color: white;
            -fx-border-color: #ddd;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            """);
        return row;
    }

    private String formatStatus(String s) {
        if (s == null || s.isBlank()) return "WANT_TO_READ";
        return s;
    }


    private void refreshReadingSessionsForSelectedBook() {
        refreshReadingSessionsForBook(selectedBook);
    }

    private void refreshReadingSessionsForBook(Book book) {
        if (readingSessionsListView == null) return;

        if (book == null || book.getId() == null) {
            clearSessionsList("Select a book to see sessions");
            return;
        }

        try {
            var sessions = readingSessionDao.findByReaderIdAndBookId(CURRENT_READER_ID, book.getId());
            readingSessionsListView.getItems().setAll(sessions);

            if (sessions.isEmpty()) {
                readingSessionsListView.setPlaceholder(new Label("No sessions for this book yet"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load sessions:\n" + ex.getMessage()).showAndWait();
        }
    }

    private void clearSessionsList(String placeholder) {
        if (readingSessionsListView == null) return;
        readingSessionsListView.getItems().clear();
        readingSessionsListView.setPlaceholder(new Label(placeholder));
    }

    private void openCreateReadingSession(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/booklib/ReadingSessionView.fxml"));
            Parent root = loader.load();

            ReadingSessionController controller = loader.getController();
            controller.setBook(book);

            Stage stage = new Stage();
            stage.setTitle("New Reading Session");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot open ReadingSessionView.fxml:\n" + e.getMessage()).showAndWait();
        }
    }

    private void openEditReadingSession(ReadingSession session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/booklib/ReadingSessionView.fxml"));
            Parent root = loader.load();

            ReadingSessionController controller = loader.getController();
            controller.setBook(session.getBook());
            controller.setEditingSession(session);

            Stage stage = new Stage();
            stage.setTitle("Edit Reading Session");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot open ReadingSessionView.fxml:\n" + e.getMessage()).showAndWait();
        }
    }

    private void showBookInfo(Book b) {
        String msg =
                "Title: " + nullToDash(b.getTitle()) + "\n" +
                        "Author: " + nullToDash(b.getAuthor()) + "\n" +
                        "Pages: " + (b.getPages() != null ? b.getPages() : "-") + "\n" +
                        "Genre: " + nullToDash(b.getGenre()) + "\n" +
                        "Language: " + nullToDash(b.getLanguage());
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private static String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private void loadBooksFromResourcesCsv() throws IOException {
        try (var in = getClass().getResourceAsStream("/booklib/books.csv")) {
            if (in == null) throw new IOException("Resource /booklib/books.csv not found");
            var tmp = Files.createTempFile("books", ".csv");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            bookDao.loadFromCsv(tmp.toFile());
        }
    }

    private static boolean containsById(ObservableList<Book> list, Long id) {
        for (Book b : list) {
            if (safeEqId(b.getId(), id)) return true;
        }
        return false;
    }

    private static boolean safeEqId(Long a, Long b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
