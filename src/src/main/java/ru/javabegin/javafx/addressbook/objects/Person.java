package ru.javabegin.javafx.addressbook.objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// объект для заполнения таблицы tableView
public class Person {

    // SimpleStringProperty для автоматического обновления столбцов tableView
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty fio = new SimpleStringProperty("");
    private SimpleStringProperty phone = new SimpleStringProperty("");


    public Person() {
    }

    public Person(String fio, String phone) {
        this.fio = new SimpleStringProperty(fio);
        this.phone = new SimpleStringProperty(phone);
    }


    public Person(int id, String fio, String phone) {
        this.fio = new SimpleStringProperty(fio);
        this.phone = new SimpleStringProperty(phone);
        this.id = new SimpleIntegerProperty(id);
    }

    public String getFio() {
        return fio.get();
    }

    public void setFio(String fio) {
        this.fio.set(fio);
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }


    public SimpleStringProperty fioProperty() {
        return fio;
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }


    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }


    @Override
    public String toString() {
        return "Person{" +
                "fio='" + fio + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

