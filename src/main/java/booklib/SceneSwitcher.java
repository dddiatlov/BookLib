package booklib;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * УТИЛИТА ДЛЯ ПЕРЕКЛЮЧЕНИЯ МЕЖДУ СЦЕНАМИ В JAVAFX
 *
 * НАЗНАЧЕНИЕ:
 * 1. Инкапсулирует логику загрузки FXML файлов и смены сцен
 * 2. Упрощает навигацию между экранами приложения
 * 3. Обрабатывает ошибки загрузки FXML
 *
 * ПРИНЦИП РАБОТЫ:
 * - Получает Stage из любого элемента текущей сцены
 * - Загружает новый FXML файл и устанавливает его как корневой элемент
 * - Заменяет текущую сцену на новую
 */
public final class SceneSwitcher {

    // Приватный конструктор - нельзя создать экземпляр (утилитарный класс)
    private SceneSwitcher() {}

    /**
     * ПЕРЕКЛЮЧЕНИЕ НА НОВУЮ СЦЕНУ
     *
     * @param fxmlResourcePath путь к FXML файлу относительно classpath (например "/booklib/LoginView.fxml")
     * @param anyNodeFromCurrentScene любой Node (элемент UI) из текущей сцены
     *
     * АЛГОРИТМ:
     * 1. Получаем URL ресурса FXML
     * 2. Загружаем FXML через FXMLLoader
     * 3. Получаем Stage из переданного Node
     * 4. Устанавливаем новую сцену и показываем Stage
     */
    public static void switchTo(String fxmlResourcePath, Node anyNodeFromCurrentScene) {
        try {
            // Получаем URL ресурса FXML
            var url = SceneSwitcher.class.getResource(fxmlResourcePath);
            if (url == null) {
                throw new IllegalArgumentException("FXML not found on classpath: " + fxmlResourcePath);
            }

            // Загружаем FXML файл
            Parent root = FXMLLoader.load(url);

            // Получаем Stage из любого элемента текущей сцены
            Stage stage = (Stage) anyNodeFromCurrentScene.getScene().getWindow();

            // Создаем новую сцену и устанавливаем ее в Stage
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            // Показываем пользователю понятное сообщение об ошибке
            Alerts.error("UI error", "Cannot open screen: " + fxmlResourcePath + "\n" + e.getMessage());
        }
    }
}