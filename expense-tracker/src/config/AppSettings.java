package config;

public class AppSettings {
    // singleton pattern : one shared settings object
    private static final AppSettings INSTANCE = new AppSettings();

    private String currency = "MDL";
    private String defaultCategory = "general";

    // private, nobody can write new appsettings
    private AppSettings() {}

    public static AppSettings getInstance() {
        return INSTANCE;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) {
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency cannot be empty.");
        this.currency = currency.trim().toUpperCase();
    }

    public String getDefaultCategory() { return defaultCategory; }
    public void setDefaultCategory(String c) {
        if (c == null || c.isBlank())
            throw new IllegalArgumentException("Category cannot be empty.");
        this.defaultCategory = c.trim().toLowerCase();
    }
}