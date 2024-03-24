package ru.devmark.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            insertOperation(userId, 1, amount); // 1 - тип операции "пополнение счета"
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
            insertOperation(userId, 2, amount); // 2 - тип операции "снятие со счета"
        } catch (SQLException e) {
            throw new RuntimeException("Error taking money from user with ID " + userId, e);
        }
    }

    public void transferMoney(int fromUserId, int toUserId, double amount) {
        double fromUserBalance = getBalance(fromUserId);
        if (fromUserBalance < amount) {
            throw new IllegalArgumentException("Not enough funds to transfer from user with ID " + fromUserId);
        }
        takeMoney(fromUserId, amount);
        putMoney(toUserId, amount);
        insertOperation(fromUserId, 4, amount); // 4 - тип операции "перевод другому клиенту"
        insertOperation(toUserId, 3, amount); // 3 - тип операции "перевод от другого клиента вам"
    }

    public List<String> getOperationList(int userId, String startDate, String endDate) {
        List<String> operations = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT date, operation_type, amount FROM operations WHERE user_id = ?");
        if (startDate != null && !startDate.isEmpty()) {
            query.append(" AND date >= TO_DATE(?, 'YYYY-MM-DD')");
        }
        if (endDate != null && !endDate.isEmpty()) {
            query.append(" AND date <= TO_DATE(?, 'YYYY-MM-DD')");
        }
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query.toString())) {
            int parameterIndex = 1;
            statement.setInt(parameterIndex++, userId);
            if (startDate != null && !startDate.isEmpty()) {
                statement.setString(parameterIndex++, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                statement.setString(parameterIndex++, endDate);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String date = resultSet.getString("date");
                    int operationType = resultSet.getInt("operation_type");
                    String type = getOperationTypeDescription(operationType);
                    double amount = resultSet.getDouble("amount");
                    operations.add("Date: " + date + ", Operation Type: " + type + ", Amount: " + amount);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching operation list for user with ID " + userId, e);
        }
        return operations;
    }

    private void insertOperation(int userId, int operationType, double amount) {
        String insertQuery = "INSERT INTO operations (user_id, operation_type, amount) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, userId);
            statement.setInt(2, operationType);
            statement.setDouble(3, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting operation for user with ID " + userId, e);
        }
    }

    private String getOperationTypeDescription(int operationType) {
        switch (operationType) {
            case 1:
                return "Пополнение счета";
            case 2:
                return "Снятие со счета";
            case 3:
                return "Перевод другому клиенту";
            case 4:
                return "Перевод от другого клиента вам";
            default:
                return "Неизвестный тип операции";
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

        // Пример вызова метода transferMoney
        int fromUserId = 1;
        int toUserId = 2;
        double transferAmount = 50;
        dbManager.transferMoney(fromUserId, toUserId, transferAmount);
        System.out.println("Transferred " + transferAmount + " from user " + fromUserId + " to user " + toUserId);

        // Пример вызова метода getOperationList
        String startDate = "2022-01-01";
        String endDate = "2022-01-31";
        List<String> operationList = dbManager.getOperationList(userId, startDate, endDate);
        System.out.println("Operation List for user " + userId + " from " + startDate + " to " + endDate + ":");
        for (String operation : operationList) {
            System.out.println(operation);
        }
    }
}