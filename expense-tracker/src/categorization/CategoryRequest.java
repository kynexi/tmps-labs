package categorization;

import model.TransactionType;

public record CategoryRequest(String description, double amount, TransactionType type) { }