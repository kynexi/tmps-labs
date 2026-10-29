package model;

import java.time.LocalDate;

/**
 * A single income or expense.
 * It only holds data and checks that the data is valid. No printing here (SRP).
 */
public class Transaction {
    private final double amount;
    private final String description;
    private final TransactionType type;
    private final LocalDate date;
    private final String category;

    public Transaction(double amount, String description, TransactionType type, LocalDate date, String category) {
        // "!(amount > 0)" also rejects NaN, which "amount <= 0" would let through
        if (!(amount > 0) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        this.amount = amount;
        this.description = description.trim();
        this.type = type;
        this.date = date;
        // lowercase so "Food" and "food" are the same category in the summary
        this.category = (category == null || category.isBlank()) ? "general" : category.trim().toLowerCase();
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }
}