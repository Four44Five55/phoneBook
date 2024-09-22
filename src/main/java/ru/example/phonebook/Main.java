package ru.example.phonebook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage; // главное окно приложения

    private VBox currentRoot;

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

            primaryStage.setTitle("Адресная книга");

            Scene scene = new Scene(currentRoot, 300, 275);
            primaryStage.setScene(scene);
            primaryStage.setMinHeight(700);
            primaryStage.setMinWidth(600);
            primaryStage.show(); // отобразить окно

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        System.exit(0);
    }
}