package com.example.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CATEGORY = 0;
    private static final int VIEW_TYPE_EXPENSE = 1;

    private List<Object> groupedItems; // Список заголовков (категорий) и расходов
    private OnItemLongClickListener onItemLongClickListener;

    public ExpensesAdapter(List<Object> groupedItems) {
        this.groupedItems = groupedItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (groupedItems.get(position) instanceof String) {
            return VIEW_TYPE_CATEGORY; // Категория
        } else {
            return VIEW_TYPE_EXPENSE; // Расход
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_CATEGORY) {
            String category = (String) groupedItems.get(position);

            ((CategoryViewHolder) holder).categoryTextView.setText(category);
        } else {
            Expenses expense = (Expenses) groupedItems.get(position);
            ExpenseViewHolder expenseHolder = (ExpenseViewHolder) holder;

            expenseHolder.categoryTextView.setText(expense.getCategory());
            expenseHolder.descriptionTextView.setText(expense.getDescription());
            expenseHolder.amountTextView.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));

            // Форматирование даты
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String dateString = dateFormat.format(new Date(expense.getDate()));
            expenseHolder.dateTextView.setText(dateString);

            // Долгое нажатие
            holder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(expense);
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return groupedItems.size();
    }

    public void updateGroupedExpenses(List<Object> newGroupedItems) {
        this.groupedItems = newGroupedItems;
        notifyDataSetChanged();
    }

    public Object getItemAtPosition(int position) {
        return groupedItems.get(position);
    }

    // Интерфейс для долгого нажатия
    public interface OnItemLongClickListener {
        void onItemLongClick(Expenses expense);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    // ViewHolder для категории
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
        }
    }

    // ViewHolder для расхода
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView, descriptionTextView, amountTextView, dateTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            amountTextView = itemView.findViewById(R.id.amount_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
        }
    }
}