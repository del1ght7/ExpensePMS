package com.example.expense;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class Registration extends AppCompatActivity {

    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Инициализация базы данных
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "expenses_database")
                .allowMainThreadQueries()
                .build();

        // Инициализация UI-компонентов
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.register_button);

        // Обработчик кнопки регистрации
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Проверка ввода
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(Registration.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Registration.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка на существование пользователя
        if (db.userDao().getUserByUsername(username) != null) {
            Toast.makeText(Registration.this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создание нового пользователя
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);

        // Сохранение пользователя в базу данных
        db.userDao().insert(newUser);

        Toast.makeText(Registration.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

        // Закрытие активности (возврат на экран логина)
        finish();
    }
}