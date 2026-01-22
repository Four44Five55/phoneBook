package ru.example.phonebook.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteConnection {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {


        // Если connection создается первый раз или ранее был закрыт - то нужно открыть новый connection
        // Самое главное - открывать соединение только по необходимости, а не держать его постоянно открытым (иначе вы будете блокировать файл БД)
        if (connection == null || connection.isClosed()){
            try {

                String url = "jdbc:sqlite:db"+ File.separator+"phonebook.db";
                connection = DriverManager.getConnection(url);

            } catch (SQLException ex) {
                Logger.getLogger(SQLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return connection;
    }

}
