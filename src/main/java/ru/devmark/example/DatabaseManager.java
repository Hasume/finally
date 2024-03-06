package ru.devmark.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Deni7822";

    public double getBalance(int userId) {
        String query = "SELECT balance FROM users WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                } else {
                    throw new RuntimeException("User with ID " + userId + " not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching balance for user with ID " + userId, e);
        }
    }

    public void putMoney(int userId, double amount) {
        String updateQuery = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setDouble(1, amount);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding money to user with ID " + userId, e);
        }
    }

    public void takeMoney(int userId, double amount) {
        String updateQuery = "UPDATE users SET balance = balance - ? WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setDouble(1, amount);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error taking money from user with ID " + userId, e);
        }
    }

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();

        // Пример вызова метода getBalance для пользователя с ID = 1
        int userId = 1;
        double balance = dbManager.getBalance(userId);
        System.out.println("Balance for user " + userId + ": " + balance);

        // Пример вызова методов putMoney и takeMoney
        dbManager.putMoney(userId, 100);
        System.out.println("Added 100 to user " + userId + "'s balance");

        dbManager.takeMoney(userId, 50);
        System.out.println("Took 50 from user " + userId + "'s balance");
    }
}