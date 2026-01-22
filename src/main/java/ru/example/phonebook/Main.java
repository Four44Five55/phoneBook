package ru.example.phonebook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.example.phonebook.controller.MainController;

import java.io.*;

public class Main extends Application {

    private Stage primaryStage; // главное окно приложения

    private VBox currentRoot;

    private MainController mainController;

    // Файл настроек
    private File getSettingsFile() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".phonebook-settings.properties");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createGUI();
    }


    public static void main(String[] args) {
        launch(args);
    }

    // загрузка fxml и отображение первого окна
    private void createGUI() {


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));

        try {

            // ссылка на корневой виз. элемент из fxml
            currentRoot = fxmlLoader.load();
            mainController = fxmlLoader.getController();

            primaryStage.setTitle("Телефонный справочник");

            Scene scene = new Scene(currentRoot, 1000, 700);
            primaryStage.setScene(scene);
            primaryStage.setMinHeight(700);
            primaryStage.setMinWidth(800);

            // Загружаем сохранённый размер окна
            loadWindowSize();

            primaryStage.show(); // отобразить окно

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Загружает сохранённый размер окна
    private void loadWindowSize() {
        File settingsFile = getSettingsFile();
        if (!settingsFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("windowWidth=")) {
                    double width = Double.parseDouble(line.substring("windowWidth=".length()));
                    primaryStage.setWidth(width);
                } else if (line.startsWith("windowHeight=")) {
                    double height = Double.parseDouble(line.substring("windowHeight=".length()));
                    primaryStage.setHeight(height);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Сохраняет размер окна
    private void saveWindowSize() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getSettingsFile(), true))) { // append mode
            writer.println("windowWidth=" + primaryStage.getWidth());
            writer.println("windowHeight=" + primaryStage.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mainController != null) {
            mainController.saveColumnWidths();
        }
        saveWindowSize(); // сохраняем размер окна
        System.exit(0);
    }
}