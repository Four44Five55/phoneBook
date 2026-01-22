module ru.example.phonebook {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    requires java.prefs;

    opens ru.example.phonebook.controller to javafx.fxml;
    opens ru.example.phonebook to javafx.fxml;
    opens ru.example.phonebook.objects to javafx.fxml, javafx.base;

    exports ru.example.phonebook;
    exports ru.example.phonebook.controller;
    exports ru.example.phonebook.objects;
}