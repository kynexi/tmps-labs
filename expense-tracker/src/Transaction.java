public class Transaction {
    private double amount;
    private String description;
    private TransactionType type;
    private String category;
    private String date;

    Transaction(double amount, String description, TransactionType type, String date, String category) {
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.date = date;
        this.category = category;
    }

    public void display() {
        System.out.println("Description: " + this.description);
        System.out.println("Category: " + this.category);
        System.out.println("Date: " + this.date);
        System.out.println("Amount: " + this.amount);
        System.out.println("Type: " + this.type);
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

}
