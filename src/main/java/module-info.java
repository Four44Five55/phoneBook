module ru.example.phonebook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ru.example.phonebook to javafx.fxml;
    opens ru.example.phonebook.objects to javafx.base;

    exports ru.example.phonebook;
    exports ru.example.phonebook.controller;
}