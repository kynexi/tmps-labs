import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Transaction t1 = new Transaction(80, "fidesco", TransactionType.INCOME, "16/09/2026", "entertaimanet");
        Transaction t2 = new Transaction(50, "gambling", TransactionType.EXPENSE, "16/09/2026", "entertaimanet");
        Transaction t3 = new Transaction(45, "gambling", TransactionType.EXPENSE, "16/09/2026", "entertaimanet" );

        TransactionService transactions = new TransactionService();
        transactions.addTransaction(t1);
        transactions.addTransaction(t2);
        transactions.addTransaction(t3);


        System.out.println("Balance: " + transactions.getBalance());
        System.out.println();

        for(Transaction t: transactions.getTransactions()) {
            t.display();
            System.out.println();
        }

        transactions.getExpensesByCategory("entertaimanet");
        transactions.getIncomeByCategory("entertaimanet");

    }
}

