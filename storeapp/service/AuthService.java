package com.example.storeapp.service;

import com.example.storeapp.model.Role;
import com.example.storeapp.model.User;
import com.example.storeapp.util.HashUtil;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    public AuthService() {}

    // 🔐 LOGIN
    public Optional<User> login(String username, String password) {
        try (Connection conn = Database.connect()) {

            var stmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?"
            );

            stmt.setString(1, username.trim());
            System.out.println(HashUtil.sha256("user123"));
            stmt.setString(2, HashUtil.sha256(password));

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new User(
                        rs.getInt("id"), // UUID как String
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        Role.valueOf(rs.getString("role"))
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // 📝 REGISTER
    public String register(String fullName, String email, String username, String password) {

        if (fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            return "Заполни все поля.";
        }

        // --- ДОБАВЛЕНА ПРОВЕРКА EMAIL ---
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Введите корректный email (пример: name@mail.ru).";
        }

        // --- ДОБАВЛЕНА ПРОВЕРКА ЛОГИНА (только буквы, цифры и _, от 3 до 20) ---
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            return "Логин должен содержать от 3 до 20 символов (буквы, цифры, _).";
        }

        if (password.length() < 6) {
            return "Пароль должен быть не короче 6 символов.";
        }

        try (Connection conn = Database.connect()) {

            // 🔍 Проверка на существование
            var check = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            check.setString(1, username.trim());

            var rs = check.executeQuery();
            if (rs.next()) {
                return "Пользователь с таким логином уже существует.";
            }

            // ➕ Добавление
            var stmt = conn.prepareStatement(
                    "INSERT INTO users(username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)"
            );

            stmt.setString(1, username.trim());
            stmt.setString(2, HashUtil.sha256(password));
            stmt.setString(3, fullName.trim());
            stmt.setString(4, email.trim());
            stmt.setString(5, "USER");

            stmt.executeUpdate();

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка БД";
        }
    }
}