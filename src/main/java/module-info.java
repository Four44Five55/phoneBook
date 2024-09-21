module ru.example.phonebook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens ru.example.phonebook to javafx.fxml;
    exports ru.example.phonebook;
}