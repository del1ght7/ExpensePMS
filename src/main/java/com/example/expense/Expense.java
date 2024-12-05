package com.example.expense;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Expense extends AppCompatActivity {

    private Spinner categorySpinner;
    private EditText newCategoryInput;
    private EditText amountInput;
    private EditText descriptionInput;
    private EditText dateInput;
    private Button saveExpenseButton;
    private AppDatabase db;

    private List<String> categories; // Список категорий
    private Expenses currentExpense; // Текущий расход (для редактирования)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense);

        // Настройка отступов для системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация базы данных
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "expenses_database")
                .allowMainThreadQueries()
                .build();

        // Инициализация UI-компонентов
        categorySpinner = findViewById(R.id.category_spinner);
        newCategoryInput = findViewById(R.id.new_category_input);
        amountInput = findViewById(R.id.amount_input);
        descriptionInput = findViewById(R.id.description_input);
        dateInput = findViewById(R.id.date_input);
        saveExpenseButton = findViewById(R.id.save_expense_button);

        // Загрузка категорий из базы данных
        loadCategories();

        // Настройка Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Проверяем, пришёл ли ID расхода для редактирования
        int expenseId = getIntent().getIntExtra("expenseId", -1);
        if (expenseId != -1) {
            // Если есть ID, загружаем расход из базы данных
            currentExpense = db.expenseDao().getExpenseById(expenseId);
            if (currentExpense != null) {
                fillExpenseData(); // Заполняем данные в форму
            }
        }

        // Настройка кнопки "Сохранить"
        saveExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String newCategory = newCategoryInput.getText().toString().trim();
                String amountStr = amountInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String dateStr = dateInput.getText().toString().trim();

                // Если пользователь ввел новую категорию, используем её
                if (!TextUtils.isEmpty(newCategory)) {
                    selectedCategory = newCategory;
                    if (!categories.contains(newCategory)) {
                        categories.add(newCategory); // Добавляем новую категорию в список
                        adapter.notifyDataSetChanged(); // Обновляем Spinner
                    }
                }

                // Валидация данных
                if (TextUtils.isEmpty(selectedCategory) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(dateStr)) {
                    Toast.makeText(Expense.this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        long date;

                        // Преобразование даты в миллисекунды
                        if (!TextUtils.isEmpty(dateStr)) {
                            String[] dateParts = dateStr.split("-");
                            int day = Integer.parseInt(dateParts[0]);
                            int month = Integer.parseInt(dateParts[1]) - 1; // Месяцы начинаются с 0
                            int year = Integer.parseInt(dateParts[2]);
                            date = new java.util.GregorianCalendar(year, month, day).getTimeInMillis();
                        } else {
                            date = System.currentTimeMillis(); // Текущая дата
                        }

                        if (currentExpense == null) {
                            // Создание нового объекта расхода
                            Expenses expense = new Expenses();
                            expense.setCategory(selectedCategory);
                            expense.setAmount(amount);
                            expense.setDescription(description);
                            expense.setDate(date);
                            expense.setUserId(1); // В реальном приложении userId будет передаваться
                            db.expenseDao().insert(expense);
                            Toast.makeText(Expense.this, "Расход добавлен!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Обновление существующего расхода
                            currentExpense.setCategory(selectedCategory);
                            currentExpense.setAmount(amount);
                            currentExpense.setDescription(description);
                            currentExpense.setDate(date);
                            db.expenseDao().update(currentExpense);
                            Toast.makeText(Expense.this, "Расход обновлён!", Toast.LENGTH_SHORT).show();
                        }

                        // Передача результата в MainActivity
                        setResult(RESULT_OK);

                        // Закрытие активности
                        finish();

                    } catch (NumberFormatException e) {
                        Toast.makeText(Expense.this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Метод для загрузки категорий из базы данных
    private void loadCategories() {
        List<Expenses> expensesList = db.expenseDao().getAllExpenses();
        Set<String> categorySet = new HashSet<>();
        for (Expenses expense : expensesList) {
            categorySet.add(expense.getCategory());
        }
        categories = new ArrayList<>(categorySet);
        categories.add(0, "Выберите категорию"); // Добавляем опцию по умолчанию
    }

    // Заполнение данных расхода в форму
    private void fillExpenseData() {
        categorySpinner.setSelection(categories.indexOf(currentExpense.getCategory()));
        amountInput.setText(String.valueOf(currentExpense.getAmount()));
        descriptionInput.setText(currentExpense.getDescription());

        // Преобразование даты в строку
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy");
        dateInput.setText(dateFormat.format(new java.util.Date(currentExpense.getDate())));
    }
}