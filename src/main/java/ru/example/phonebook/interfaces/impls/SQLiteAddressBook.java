package ru.example.phonebook.interfaces.impls;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.example.phonebook.db.SQLiteConnection;
import ru.example.phonebook.interfaces.AddressBook;
import ru.example.phonebook.objects.Person;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


// Реализация хранения данных в БД SQLite
// Используется стандартный JDBC (нативные SQL запросы, не Hibernate)
public class SQLiteAddressBook implements AddressBook {

    // Коллекция, которая может уведомлять слушателей о своих изменениях
    // Setter не используем, т.к. коллекция заполняется методами БД
    private ObservableList<Person> personList = FXCollections.observableArrayList();

    public SQLiteAddressBook() {
        personList = findAll(); // сразу загружаем все данные адресной книги
    }

    @Override
    public boolean add(Person person) {
        try (Connection con = SQLiteConnection.getConnection(); PreparedStatement statement = con.prepareStatement("insert into person(fio, phone) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, person.getFio());
            statement.setString(2, person.getPhone());

            // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
            int result = statement.executeUpdate();
            if (result > 0) {
                int id = statement.getGeneratedKeys().getInt(1);// получить сгенерированный id новой записи
                person.setId(id);
                personList.add(person); // добавить в коллекцию, чтобы обновить данные в tableView
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteAddressBook.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SQLiteAddressBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }



    @Override
    public boolean update(Person person) {

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
        try (Connection con = SQLiteConnection.getConnection(); PreparedStatement statement = con.prepareStatement("update person set fio=?, phone=? where id=?")) {
            statement.setString(1, person.getFio());
            statement.setString(2, person.getPhone());
            statement.setInt(3, person.getId());

            int result = statement.executeUpdate();
            if (result > 0) {
                // обновление в коллекции для tableView происходит автоматически, после нажатия ОК в окне редактирования. Поэтому тут никакие доп. действия не производим
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteAddressBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    // поиск по включению текста в ФИО
    @Override
    public ObservableList<Person> find(String text) {

        personList.clear(); // старые данные коллекции нужно очистить сначала

        // используем try-with-resource, чтобы объекты закрывались автоматически и не нужно было добавлять блок finally
        try (Connection con = SQLiteConnection.getConnection(); PreparedStatement statement = con.prepareStatement("select * from person where fio like ? or phone like ?");) {

            String searchStr = "%" + text + "%";

            statement.setString(1, searchStr);
            statement.setString(2, searchStr);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setFio(rs.getString("fio"));
                person.setPhone(rs.getString("phone"));
                personList.add(person); // добавляем в коллекцию, чтобы обновить данные в tableView
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteAddressBook.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                personList.add(person); // добавляем в коллекцию, чтобы обновить данные в tableView
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteAddressBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        return personList;
    }



    public ObservableList<Person> getPersonList() {
        return personList;
    }


}
