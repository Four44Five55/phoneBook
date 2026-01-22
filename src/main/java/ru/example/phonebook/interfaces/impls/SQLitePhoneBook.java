package ru.example.phonebook.interfaces.impls;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.example.phonebook.db.SQLiteConnection;
import ru.example.phonebook.interfaces.PhoneBook;
import ru.example.phonebook.objects.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


// Реализация хранения данных в БД SQLite
// Используется стандартный JDBC (нативные SQL запросы, не Hibernate)
public class SQLitePhoneBook implements PhoneBook {

    // Коллекция, которая может уведомлять слушателей о своих изменениях
    // Setter не используем, т.к. коллекция заполняется методами БД
    private ObservableList<Person> personList = FXCollections.observableArrayList();

    public SQLitePhoneBook() {
        personList = findAll(); // сразу загружаем все данные адресной книги
    }

    @Override
    public boolean add(Person person) {
        try (Connection con = SQLiteConnection.getConnection(); PreparedStatement statement = con.prepareStatement("insert into person(fio, phone, department, notes) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, person.getFio());
            statement.setString(2, person.getPhone());
            statement.setString(3, person.getDepartment());
            statement.setString(4, person.getNotes());

            // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
            int result = statement.executeUpdate();
            if (result > 0) {
                int id = statement.getGeneratedKeys().getInt(1);// получить сгенерированный id новой записи
                person.setId(id);
                personList.add(person); // добавить в коллекцию, чтобы обновить данные в tableView
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public boolean delete(Person person) {

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
        try (Connection con = SQLiteConnection.getConnection(); Statement statement = con.createStatement();) {
            int result = statement.executeUpdate("delete from person where id=" + person.getId());

            if (result > 0) {
                personList.remove(person); // удалить из коллекции, чтобы обновить данные в tableView
                return true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }



    @Override
    public boolean update(Person person) {

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
        try (Connection con = SQLiteConnection.getConnection(); PreparedStatement statement = con.prepareStatement("update person set fio=?, phone=?, department=?, notes=? where id=?")) {
            statement.setString(1, person.getFio());
            statement.setString(2, person.getPhone());
            statement.setString(3, person.getDepartment());
            statement.setString(4, person.getNotes());
            statement.setInt(5, person.getId());

            int result = statement.executeUpdate();
            if (result > 0) {
                // обновление в коллекции для tableView происходит автоматически, после нажатия ОК в окне редактирования. Поэтому тут никакие доп. действия не производим
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    // Нормализация текста для поиска (ё -> е, нижний регистр)
    private String normalizeForSearch(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replace("ё", "е");
    }

    // поиск по включению текста в ФИО (без учета регистра и ё=е)
    @Override
    public ObservableList<Person> find(String text) {

        ObservableList<Person> result = FXCollections.observableArrayList();
        String searchText = normalizeForSearch(text);

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок наконец
        try (Connection con = SQLiteConnection.getConnection(); Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery("select * from person");) {

            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setFio(rs.getString("fio"));
                person.setPhone(rs.getString("phone"));
                person.setDepartment(rs.getString("department"));
                person.setNotes(rs.getString("notes"));

                // фильтрация на уровне Java (корректно работает с кириллицей и ё=е)
                String department = person.getDepartment() == null ? "" : person.getDepartment();
                String notes = person.getNotes() == null ? "" : person.getNotes();
                if (normalizeForSearch(person.getFio()).contains(searchText) ||
                    normalizeForSearch(person.getPhone()).contains(searchText) ||
                    normalizeForSearch(department).contains(searchText) ||
                    normalizeForSearch(notes).contains(searchText)) {
                    result.add(person);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        personList.clear();
        personList.addAll(result);
        return personList;
    }

    // поиск всех людей в адресной книге
    @Override
    public ObservableList<Person> findAll() {
        personList.clear(); // старые данные коллекции нужно очистить сначала

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
        try (Connection con = SQLiteConnection.getConnection(); Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery("select * from person");) {
            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setFio(rs.getString("fio"));
                person.setPhone(rs.getString("phone"));
                person.setDepartment(rs.getString("department"));
                person.setNotes(rs.getString("notes"));
                personList.add(person); // добавляем в коллекцию, чтобы обновить данные в tableView
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return personList;
    }



    public ObservableList<Person> getPersonList() {
        return personList;
    }

    // Возвращает все записи из БД без изменения personList (для экспорта)
    public List<Person> findAllPeople() {
        List<Person> result = new ArrayList<>();

        try (Connection con = SQLiteConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery("select * from person")) {

            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setFio(rs.getString("fio"));
                person.setPhone(rs.getString("phone"));
                person.setDepartment(rs.getString("department"));
                person.setNotes(rs.getString("notes"));
                result.add(person);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLitePhoneBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
