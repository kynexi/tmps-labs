import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Transaction t1 = new Transaction(80, "fidesco", TransactionType.INCOME, "16/09/2026", "entertaimanet");
        Transaction t2 = new Transaction(50, "gambling", TransactionType.EXPENSE, "16/09/2026", "entertaimanet");

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(t1);
        transactions.add(t2);

        for (Transaction t : transactions) {
            System.out.println("--");
            t.display();
            System.out.println();
        }

        double balance = 0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                balance += t.getAmount();
            }
            else if (t.getType() == TransactionType.EXPENSE) {
                balance -= t.getAmount();
            }
        }

        System.out.println(balance);

    }
}

