import java.util.ArrayList;

public class TransactionService {
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void removeTransaction(int index) {
        transactions.remove(index);
    }

    public double getTotalIncome() {
        double income = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                income += t.getAmount();
            }
        }
        return income;
    }

    public double getTotalExpense() {
        double expense = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                expense += t.getAmount();
            }
        }
        return expense;
    }

    public double getBalance() {
        double balance = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                balance += t.getAmount();
            }
            else if (t.getType() == TransactionType.EXPENSE) {
                balance -= t.getAmount();
            }
        }
        return balance;
    }

    public double getExpensesByCategory(String Category){
        double expense = 0;
        for (Transaction t : transactions) {
            if (t.getCategory().equals(Category) && t.getType() == TransactionType.EXPENSE) {
                expense += t.getAmount();
            }
        }
        System.out.println("Expenses by Category: " + Category + " = " + expense);
        return expense;
    }

    public double getIncomeByCategory(String Category){
        double income = 0;
        for (Transaction t : transactions) {
            if (t.getCategory().equals(Category) && t.getType() == TransactionType.INCOME) {
                income += t.getAmount();
            }
        }
        System.out.println("Income by Category: " + Category + " = " + income);
        return income;
    }


}
