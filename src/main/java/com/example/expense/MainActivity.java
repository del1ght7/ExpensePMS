package com.example.expense;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView expensesRecyclerView;
    private ExpensesAdapter expensesAdapter;
    private Button addExpenseButton;
    private Button logoutButton;
    private Button clearAllButton;
    private TextView totalExpensesTextView;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view);
        addExpenseButton = findViewById(R.id.add_expense_button);
        logoutButton = findViewById(R.id.logout_button);
        clearAllButton = findViewById(R.id.clear_all_button);
        totalExpensesTextView = findViewById(R.id.total_expenses_text_view);

        // Настройка RecyclerView
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Загрузка расходов текущего пользователя (здесь используем userId = 1 для теста)
        int userId = 1; // В реальном приложении userId нужно передавать после логина
        loadExpenses(userId);

        // Установка свайпа для удаления и долгого нажатия
        setupItemTouchHelper(userId);

        // Кнопка для добавления нового расхода
        addExpenseButton.setOnClickListener(v -> {
            // Переход на экран добавления расхода с ожиданием результата
            startActivityForResult(new Intent(MainActivity.this, Expense.class), 1); // 1 — код запроса
        });

        // Кнопка выхода из аккаунта
        logoutButton.setOnClickListener(v -> {
            // Переход на экран логина
            startActivity(new Intent(MainActivity.this, Login.class));
            finish(); // Закрыть текущую активность
        });

        // Кнопка удаления всех расходов
        clearAllButton.setOnClickListener(v -> {
            // Диалог подтверждения удаления всех расходов
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Очистить все расходы")
                    .setMessage("Вы уверены, что хотите удалить все расходы?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        // Удаление расходов из базы данных
                        db.expenseDao().deleteExpensesByUserId(userId);
                        loadExpenses(userId); // Обновить список расходов и сумму
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int userId = 1; // Текущий пользователь (в реальном приложении будет передаваться после логина)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Обновление данных после добавления нового расхода
            loadExpenses(userId);
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Обновление данных после редактирования
            loadExpenses(userId);
        }
    }

    private void loadExpenses(int userId) {
        // Получаем список всех расходов текущего пользователя
        List<Expenses> expensesList = db.expenseDao().getExpensesByUserId(userId);

        // Группируем расходы по категориям
        Map<String, List<Expenses>> groupedExpenses = new HashMap<>();
        for (Expenses expense : expensesList) {
            String category = expense.getCategory();
            if (!groupedExpenses.containsKey(category)) {
                groupedExpenses.put(category, new ArrayList<>());
            }
            groupedExpenses.get(category).add(expense);
        }

        // Преобразуем в список для адаптера
        List<Object> groupedItems = new ArrayList<>();
        for (Map.Entry<String, List<Expenses>> entry : groupedExpenses.entrySet()) {
            String category = entry.getKey();
            List<Expenses> categoryExpenses = entry.getValue();

            // Добавляем категорию
            groupedItems.add(category + " (Итого: " + calculateCategoryTotal(categoryExpenses) + " руб.)");

            // Добавляем расходы для категории
            groupedItems.addAll(categoryExpenses);
        }

        // Устанавливаем данные в адаптер
        if (expensesAdapter == null) {
            expensesAdapter = new ExpensesAdapter(groupedItems);
            expensesRecyclerView.setAdapter(expensesAdapter);
        } else {
            expensesAdapter.updateGroupedExpenses(groupedItems);
        }

        // Обновляем общую сумму расходов
        updateTotalExpenses(userId);
    }

    // Метод для подсчета общей суммы для категории
    private double calculateCategoryTotal(List<Expenses> expenses) {
        double total = 0;
        for (Expenses expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    // Метод для подсчета и отображения общей суммы расходов
    private void updateTotalExpenses(int userId) {
        double totalExpenses = db.expenseDao().getTotalExpensesByUserId(userId);
        totalExpensesTextView.setText(String.format("Общая сумма: %.2f руб.", totalExpenses));
    }

    // Установка свайпа для удаления и долгого нажатия
    private void setupItemTouchHelper(int userId) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Object item = expensesAdapter.getItemAtPosition(position);

                if (item instanceof Expenses) {
                    Expenses expense = (Expenses) item;

                    // Удалить расход из базы
                    db.expenseDao().deleteById(expense.getId());
                    // Обновить данные
                    loadExpenses(userId);
                    Toast.makeText(MainActivity.this, "Расход удалён", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(expensesRecyclerView);

        // Добавление долгого нажатия для редактирования
        expensesAdapter.setOnItemLongClickListener(new ExpensesAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Expenses expense) {
                Intent intent = new Intent(MainActivity.this, Expense.class);
                intent.putExtra("expenseId", expense.getId()); // Передача ID расхода
                startActivityForResult(intent, 2); // Код запроса для обновления
            }
        });
    }
}
