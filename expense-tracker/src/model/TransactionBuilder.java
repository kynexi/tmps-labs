package model;

import java.time.LocalDate;

public class TransactionBuilder {
    private double amount;
    private String description;
    private TransactionType type;
    // defaults for optional fields
    private LocalDate date = LocalDate.now();
    private String category = AppSettings.getInstance().getDefaultCategory();

    // each setter returns "this" so calls can be chained
    public TransactionBuilder amount(double amount) { this.amount = amount; return this; }
    public TransactionBuilder description(String d) { this.description = d; return this; }
    public TransactionBuilder type(TransactionType t) { this.type = t; return this; }

    public TransactionBuilder date(LocalDate date) {
        if (date != null) this.date = date;          // null keeps "today"
        return this;
    }

    public TransactionBuilder category(String category) {
        if (category != null && !category.isBlank()) this.category = category; // empty keeps default
        return this;
    }

    // your Transaction constructor still validates everything
    public Transaction build() {
        return new Transaction(amount, description, type, date, category);
    }
}