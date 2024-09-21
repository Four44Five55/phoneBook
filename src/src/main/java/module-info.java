module ru.javabegin.javafx.addressbook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ru.javabegin.javafx.addressbook.controller to javafx.fxml;
    opens ru.javabegin.javafx.addressbook.objects to javafx.base;

    exports ru.javabegin.javafx.addressbook;
}