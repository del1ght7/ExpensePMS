package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class Login extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton; // Кнопка для перехода на регистрацию
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация UI-компонентов
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button); // Инициализируем кнопку регистрации

        // Инициализация базы данных
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "expenses_database")
                .allowMainThreadQueries()
                .build();

        // Обработчик нажатия кнопки "Войти"
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                } else {
                    User user = db.userDao().getUserByUsernameAndPassword(username, password);
                    if (user != null) {
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Обработчик нажатия кнопки "Зарегистрироваться"
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на экран регистрации
                startActivity(new Intent(Login.this, Registration.class));
            }
        });
    }
}