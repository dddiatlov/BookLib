package booklib.readers;

import booklib.Alerts;
import booklib.Factory;
import booklib.SceneSwitcher;
import booklib.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * КОНТРОЛЛЕР ОКНА АВТОРИЗАЦИИ (ЛОГИН/РЕГИСТРАЦИЯ)
 * Управляет окном входа в приложение (LoginView.fxml)
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Сервис аутентификации с внедренной зависимостью ReaderDao
    private final AuthService authService = new AuthService(Factory.INSTANCE.getReaderDao());

    /**
     * ОБРАБОТЧИК КНОПКИ "LOGIN"
     * 1. Выполняет аутентификацию через AuthService
     * 2. Сохраняет пользователя в сессии
     * 3. Переходит на главный экран
     */
    @FXML
    public void onLogin(ActionEvent e) {
        try {
            // Аутентификация пользователя
            var reader = authService.login(usernameField.getText(), passwordField.getText());

            // Сохраняем текущего пользователя в статической сессии
            Session.setCurrentReader(reader);

            // Переключаемся на главный экран приложения
            SceneSwitcher.switchTo("/booklib/MainView.fxml", (Node) e.getSource());

        } catch (Exception ex) {
            Alerts.error("Login failed", ex.getMessage());
        }
    }

    /**
     * ОБРАБОТЧИК КНОПКИ "REGISTER"
     * 1. Регистрирует нового пользователя
     * 2. Показывает сообщение об успехе
     * 3. Оставляет пользователя на том же экране для входа
     */
    @FXML
    public void onRegister(ActionEvent e) {
        try {
            authService.register(usernameField.getText(), passwordField.getText());
            Alerts.info("Success", "User registered. Now you can login.");
        } catch (Exception ex) {
            Alerts.error("Register failed", ex.getMessage());
        }
    }
}