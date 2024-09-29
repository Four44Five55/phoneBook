package ru.example.phonebook.interfaces;


import javafx.collections.ObservableList;
import ru.example.phonebook.objects.Person;


// Интерфейс для доступа к данным (API для запросов в БД)
// Используется паттерн DAO - Data Access Object, чтобы можно было продключать любой источник данных, а API не менялось
// Минимальный функционал CRUD - create read update delete
public interface AddressBook {

    boolean add(Person person);

    boolean update(Person person);

    boolean delete(Person person);

    ObservableList<Person> findAll(); // поиск всех людей в адресной книге

    ObservableList<Person> find(String text); // поиск по включению текста в ФИО

}
